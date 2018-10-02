package scot.mygov.housing.forms.modeltenancy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scot.mygov.housing.forms.FieldExtractor;
import scot.mygov.housing.forms.modeltenancy.model.AgentOrLandLord;
import scot.mygov.housing.forms.modeltenancy.model.CommunicationsAgreement;
import scot.mygov.housing.forms.modeltenancy.model.DepositSchemeAdministrator;
import scot.mygov.housing.forms.modeltenancy.model.DepositSchemeAdministrators;
import scot.mygov.housing.forms.modeltenancy.model.FurnishingType;
import scot.mygov.housing.forms.modeltenancy.model.ModelTenancy;
import scot.mygov.housing.forms.modeltenancy.model.OptionalTerms;
import scot.mygov.housing.forms.modeltenancy.model.Person;
import scot.mygov.housing.forms.modeltenancy.model.RentPaymentFrequency;
import scot.mygov.housing.forms.modeltenancy.model.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static scot.mygov.housing.forms.FieldExtractorUtils.*;
import static scot.mygov.housing.forms.FieldExtractorUtils.addressFieldsMultipleLines;

/**
 * Extract fields from a ModelTenancy object for use in a template.
 */
public class ModelTenancyFieldExtractor implements FieldExtractor<ModelTenancy> {

    private static final Logger LOG = LoggerFactory.getLogger(ModelTenancyFieldExtractor.class);

    public static final String NEWLINE = "\n";

    private DepositSchemeAdministrators depositScemeAdministrators = new DepositSchemeAdministrators();

    @Inject
    public ModelTenancyFieldExtractor() {
        // Default constructor
    }

    public Map<String, Object> extractFields(ModelTenancy tenancy) {
        Map<String, Object> fields = new HashMap<>();

        // extract the fields - extract in the order they appear in the template for clarity.
        extractTenants(tenancy, fields);
        extractLettingAgent(tenancy, fields);
        extractLandlords(tenancy, fields);
        extractCommunicationAgreement(tenancy, fields);
        extractPropertyDetails(tenancy, fields);
        fields.put("tenancyStartDate", formatDate(tenancy.getTenancyStartDate()));
        extractRent(tenancy, fields);
        extractDeposit(tenancy, fields);
        extractOptionalTerms(tenancy.getOptionalTerms(), fields);
        fields.put("endingTheTenancy", tenancy.getMustIncludeTerms().getEndingTheTenancy());

        return fields;
    }

    private void extractTenants(ModelTenancy tenancy, Map<String, Object> fields) {
        List<String> namesAndAddresses = new ArrayList<>();
        List<String> emails = new ArrayList<>();
        List<String> phones = new ArrayList<>();

        List<Person> filteredTenants = tenancy.getTenants()
                .stream()
                .filter(person -> !isEmpty(person))
                .collect(toList());

        for (int i = 0; i < filteredTenants.size(); i++) {
            Person tenant = filteredTenants.get(i);
            int tenantIndex = i + 1;
            namesAndAddresses.add(nameAndAddress(tenant, tenantIndex));
            emails.add(numberedValue(tenant.getEmail(), tenantIndex));
            phones.add(numberedValue(tenant.getTelephone(), tenantIndex));
        }

        fields.put("tenantNamesAndAddresses", namesAndAddresses.stream().collect(joining(NEWLINE)));
        fields.put("tenantEmails", emails.stream().collect(joining(NEWLINE)));
        fields.put("tenantPhoneNumbers", phones.stream().collect(joining(NEWLINE)));
    }

    private void extractLettingAgent(ModelTenancy tenancy, Map<String, Object> fields) {

        // if they chose no letting agent explicitly then remove the section
        if ("letting-agent-no".equals(tenancy.getHasLettingAgent())) {
            fields.put("showLettingAgentService", "");
        } else {
            fields.put("showLettingAgentService", " ");
        }

        // letting agent is optional
        if (tenancy.getLettingAgent() == null) {
            return;
        }

        AgentOrLandLord agent = tenancy.getLettingAgent();
        fields.put("lettingAgentName", agent.getName());
        fields.put("lettingAgentAddress", addressFieldsMultipleLines(agent.getAddress()));
        fields.put("lettingAgentEmail", naForEmpty(agent.getEmail()));
        fields.put("lettingAgentPhone", naForEmpty(agent.getTelephone()));
        fields.put("lettingAgentRegistrationNumber", agent.getRegistrationNumber());
    }

    private void extractLandlords(ModelTenancy tenancy, Map<String, Object> fields) {
        List<String> landlordNames = new ArrayList<>();
        List<String> landlordAddresses = new ArrayList<>();
        List<String> landlordEmails = new ArrayList<>();
        List<String> landlordPhones = new ArrayList<>();
        List<String> landlordRegNumbers = new ArrayList<>();

        List<AgentOrLandLord> filteredLandlords = tenancy.getLandlords()
                .stream()
                .filter(person -> !isEmpty(person))
                .collect(toList());

        for (int i = 0; i < filteredLandlords.size(); i++) {
            AgentOrLandLord landlord = filteredLandlords.get(i);
            int landlordIndex = i + 1;
            landlordNames.add(String.format("Name (%d): %s", landlordIndex, landlord.getName()));
            landlordAddresses.add(String.format("Address (%d): %s%s%s",
                    landlordIndex, NEWLINE, addressFieldsMultipleLines(landlord.getAddress()), NEWLINE));
            landlordEmails.add(numberedValue(landlord.getEmail(), landlordIndex));
            landlordPhones.add(numberedValue(landlord.getTelephone(), landlordIndex));
            landlordRegNumbers.add(
                    String.format("Registration number (Landlord %d):  %s", landlordIndex, regNumber(landlord)));
        }
        fields.put("landlordNames", landlordNames.stream().collect(joining(NEWLINE)));
        fields.put("landlordAddresses", landlordAddresses.stream().collect(joining(NEWLINE)));
        fields.put("landlordEmails", landlordEmails.stream().collect(joining(NEWLINE)));
        fields.put("landlordPhones", landlordPhones.stream().collect(joining(NEWLINE)));
        fields.put("landlordRegNumbers", landlordRegNumbers.stream().collect(joining(NEWLINE + NEWLINE)));
    }

    private void extractCommunicationAgreement(ModelTenancy tenancy, Map<String, Object> fields) {

        String communicationsAgreementHardcopy = " ";
        String communicationsAgreementEmail = " ";
        String showEmailParagraphs = " ";


        if (CommunicationsAgreement.HARDCOPY.name().equals(tenancy.getCommunicationsAgreement())) {
            communicationsAgreementHardcopy = "X";
            showEmailParagraphs = "";
        }

        if (CommunicationsAgreement.EMAIL.name().equals(tenancy.getCommunicationsAgreement())) {
            communicationsAgreementEmail = "X";
            showEmailParagraphs = " ";
        }

        fields.put("communicationsAgreementHardcopy", communicationsAgreementHardcopy);
        fields.put("communicationsAgreementEmail", communicationsAgreementEmail);
        fields.put("showEmailParagraphs", showEmailParagraphs);
    }

    private void extractPropertyDetails(ModelTenancy tenancy, Map<String, Object> fields) {

        fields.put("propertyAddress", tenancy.getPropertyAddress());
        fields.put("propertyType", tenancy.getPropertyType());
        fields.put("furnishingType", FurnishingType.describe(tenancy.getFurnishingType()));

        String rentPressureZoneString = "";
        if (isTrue(tenancy.getInRentPressureZone())) {
            rentPressureZoneString = "is";
        }

        if (isFalse(tenancy.getInRentPressureZone())) {
            rentPressureZoneString = "is not";
        }
        fields.put("rentPressureZoneString", rentPressureZoneString);

        String hmoString = "";
        String hmoContactNumber = "";
        String hmoExpiryDate = "";
        Boolean hmoRenewalApplicationSubmitted = false;
        String showHmoNotification = " ";
        String showHmoFields = " ";

        if (isTrue(tenancy.getHmoProperty())) {
            hmoString = "is";
            hmoContactNumber = tenancy.getHmo24ContactNumber();
            hmoRenewalApplicationSubmitted = tenancy.getHmoRenewalApplicationSubmitted();
            hmoExpiryDate = formatDate(tenancy.getHmoRegistrationExpiryDate());
            showHmoNotification = "";
        }

        if (isFalse(tenancy.getHmoProperty())) {
            hmoString = "is not";
            hmoContactNumber = NOT_APPLICABLE;
            hmoExpiryDate = NOT_APPLICABLE;
            showHmoFields = "";
        }

        fields.put("hmoString", hmoString);
        fields.put("hmoContactNumber", hmoContactNumber);
        fields.put("hmoRenewalApplicationSubmitted", hmoRenewalApplicationSubmitted);
        fields.put("hmoExpiryDate", hmoExpiryDate);
        fields.put("showHmoNotification", showHmoNotification);
        fields.put("showHmoFields", showHmoFields);

        extractServices(tenancy, fields);
        extractFacilities(tenancy, fields);
    }

    public void extractRent(ModelTenancy tenancy, Map<String, Object> fields) {
        String rentAmount = tenancy.getRentAmount();
        String rentPaymentFrequency = RentPaymentFrequency.description(tenancy.getRentPaymentFrequency());
        fields.put("rentAmount", rentAmount);
        fields.put("originalRentAmount", tenancy.getRentAmount());
        fields.put("rentPaymentFrequency", rentPaymentFrequency);
        fields.put("rentPaymentFrequencyDayOrDate", RentPaymentFrequency.dayOrDate(tenancy.getRentPaymentFrequency()));
        extractAdvanceOrArrears(tenancy, fields);
        fields.put("firstPaymentDate", formatDate(tenancy.getFirstPaymentDate()));
        fields.put("firstPaymentAmount", tenancy.getFirstPaymentAmount());
        fields.put("firstPaymentPeriodStart", formatDate(tenancy.getTenancyStartDate()));
        fields.put("firstPaymentPeriodEnd", formatDate(tenancy.getFirstPaymentPeriodEnd()));
        fields.put("rentPaymentSchedule", tenancy.getRentPaymentSchedule());
        fields.put("rentPaymentMethod", tenancy.getRentPaymentMethod());
    }

    public void extractAdvanceOrArrears(ModelTenancy tenancy, Map<String, Object> fields) {
        String advanceOrArrears = "";
        if (isTrue(tenancy.getRentPayableInAdvance())) {
            advanceOrArrears = "advance";
        }
        if (isFalse(tenancy.getRentPayableInAdvance())) {
            advanceOrArrears = "arrears";
        }
        fields.put("advanceOrArrears", advanceOrArrears);
    }

    public void extractDeposit(ModelTenancy tenancy, Map<String, Object> fields) {
        fields.put("depositAmount", tenancy.getDepositAmount());
        fields.put("depositSchemeAdministrator", tenancy.getTenancyDepositSchemeAdministrator());
        DepositSchemeAdministrator depositSchemeAdministrator =
                depositScemeAdministrators.forName(tenancy.getTenancyDepositSchemeAdministrator());
        String depositSchemeAdministratorContactDetails = depositSchemeAdministratorContactDetails(depositSchemeAdministrator);
        fields.put("depositSchemeContactDetails", depositSchemeAdministratorContactDetails);
    }

    private void extractServices(ModelTenancy tenancy, Map<String, Object> fields) {
        extractServicesList(tenancy.getServicesIncludedInRent(), fields, "servicesIncludedInRent");
        extractServicesList(tenancy.getServicesProvidedByLettingAgent(), fields, "lettingAgentServices");
        extractServicesList(tenancy.getServicesLettingAgentIsFirstContactFor(), fields, "lettingAgentPointOfContactServices");
    }

    private void extractServicesList(List<Service> services, Map<String, Object> fields, String field) {
        fields.put(field, services.stream().map(this::formatService).collect(joining(", ")));
    }

    private String formatService(Service service) {
        if (StringUtils.isEmpty(service.getValue())) {
            return service.getName();
        } else {
            return format("%s £%s", service.getName(), service.getValue());
        }
    }

    private void extractFacilities(ModelTenancy tenancy, Map<String, Object> fields) {
        fields.put("includedAreasOrFacilities",
                tenancy.getIncludedAreasOrFacilities().stream().collect(joining(", ")));
        fields.put("excludedAreasFacilities",
                tenancy.getExcludedAreasFacilities().stream().collect(joining(", ")));
        fields.put("sharedFacilities",
                tenancy.getSharedFacilities().stream().collect(joining(", ")));
    }

    private void extractOptionalTerms(OptionalTerms optionalTerms, Map<String, Object> fields) {
        try {
            BeanUtils.describe(optionalTerms)
                    .entrySet()
                    .stream()
                    .forEach(entry -> fields.put(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            LOG.warn("Failed to extract properties", e);
        }
    }

    private String regNumber(AgentOrLandLord landlord) {
        if (StringUtils.isEmpty(landlord.getRegistrationNumber())) {
            return "Pending – the Landlord will inform the Tenant of the Registration number once they have it";
        } else {
            return String.format("[%s]", landlord.getRegistrationNumber());
        }
    }

    private String depositSchemeAdministratorContactDetails(DepositSchemeAdministrator administrator) {
        if (administrator == null) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        addAll(parts,
                administrator.getWebsite(),
                administrator.getEmail(),
                administrator.getTelephone());
        return parts.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(joining("\n"));
    }

    private boolean isTrue(String str) {
        return "true".equals(str);
    }

    private boolean isFalse(String str) {
        return "false".equals(str);
    }

}
