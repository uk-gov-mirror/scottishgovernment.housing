package scot.mygov.housing.forms.modeltenancy.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import scot.mygov.housing.forms.AbstractFormModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO for model tenancy data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelTenancy extends AbstractFormModel {

    private List<Person> tenants = new ArrayList<>();
    private List<Guarantor> guarantors = new ArrayList<>();
    private AgentOrLandLord lettingAgent;
    private String hasLettingAgent = "";
    private List<AgentOrLandLord> landlords = new ArrayList<>();
    private String communicationsAgreement;
    private String propertyAddress;
    private String propertyType = "";
    private String furnishingType  = "";
    private String inRentPressureZone;
    private String hmoProperty;
    private String hmo24ContactNumber = "";
    private Boolean hmoRenewalApplicationSubmitted;
    @JsonDeserialize(using= LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hmoRegistrationExpiryDate;
    @JsonDeserialize(using= LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate tenancyStartDate;
    private String rentAmount  = "";
    private String rentPaymentFrequency = "";
    private String rentPayableInAdvance;
    @JsonDeserialize(using= LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstPaymentDate;
    private String firstPaymentAmount = "";
    @JsonDeserialize(using= LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstPaymentPeriodEnd;
    private String rentPaymentDayOrDate = "";
    private String rentPaymentSchedule = "";
    private String rentPaymentMethod = "";
    private List<Service> servicesIncludedInRent = new ArrayList<>();
    private List<Service> servicesProvidedByLettingAgent = new ArrayList<>();
    private List<Service> servicesLettingAgentIsFirstContactFor = new ArrayList<>();
    private List<String> includedAreasOrFacilities = new ArrayList<>();
    private List<String> excludedAreasFacilities = new ArrayList<>();
    private List<String> sharedFacilities = new ArrayList<>();
    private String depositAmount = "";
    private String tenancyDepositSchemeAdministrator = "";
    private OptionalTerms optionalTerms = new OptionalTerms();
    private MustIncludeTerms mustIncludeTerms = new MustIncludeTerms();
    private List<Term> additionalTerms = new ArrayList<>();
    private List<String> excludedTerms = new ArrayList<>();

    public List<Person> getTenants() {
        return tenants;
    }

    public void setTenants(List<Person> tenants) {
        this.tenants = tenants;
    }

    public List<Guarantor> getGuarantors() {
        return guarantors;
    }

    public void setGuarantors(List<Guarantor> guarantors) {
        this.guarantors = guarantors;
    }

    public AgentOrLandLord getLettingAgent() {
        return lettingAgent;
    }

    public void setLettingAgent(AgentOrLandLord lettingAgent) {
        this.lettingAgent = lettingAgent;
    }

    public String getHasLettingAgent() {
        return hasLettingAgent;
    }

    public void setHasLettingAgent(String hasLettingAgent) {
        this.hasLettingAgent = hasLettingAgent;
    }

    public List<AgentOrLandLord> getLandlords() {
        return landlords;
    }

    public void setLandlords(List<AgentOrLandLord> landlords) {
        this.landlords = landlords;
    }

    public String getCommunicationsAgreement() {
        return communicationsAgreement;
    }

    public void setCommunicationsAgreement(String communicationsAgreement) {
        this.communicationsAgreement = communicationsAgreement;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getFurnishingType() {
        return furnishingType;
    }

    public void setFurnishingType(String furnishingType) {
        this.furnishingType = furnishingType;
    }

    public String getInRentPressureZone() {
        return inRentPressureZone;
    }

    public void setInRentPressureZone(String inRentPressureZone) {
        this.inRentPressureZone = inRentPressureZone;
    }

    public String getHmoProperty() {
        return hmoProperty;
    }

    public void setHmoProperty(String hmoProperty) {
        this.hmoProperty = hmoProperty;
    }

    public String getHmo24ContactNumber() {
        return hmo24ContactNumber;
    }

    public void setHmo24ContactNumber(String hmo24ContactNumber) {
        this.hmo24ContactNumber = hmo24ContactNumber;
    }

    public Boolean getHmoRenewalApplicationSubmitted() {
        return hmoRenewalApplicationSubmitted;
    }

    public void setHmoRenewalApplicationSubmitted(Boolean hmoRenewalApplicationSubmitted) {
        this.hmoRenewalApplicationSubmitted = hmoRenewalApplicationSubmitted;
    }

    public LocalDate getHmoRegistrationExpiryDate() {
        return hmoRegistrationExpiryDate;
    }

    public void setHmoRegistrationExpiryDate(LocalDate hmoRegistrationExpiryDate) {
        this.hmoRegistrationExpiryDate = hmoRegistrationExpiryDate;
    }

    public LocalDate getTenancyStartDate() {
        return tenancyStartDate;
    }

    public void setTenancyStartDate(LocalDate tenancyStartDate) {
        this.tenancyStartDate = tenancyStartDate;
    }

    public String getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(String rentAmount) {
        this.rentAmount = rentAmount;
    }

    public String getRentPaymentFrequency() {
        return rentPaymentFrequency;
    }

    public void setRentPaymentFrequency(String rentPaymentFrequency) {
        this.rentPaymentFrequency = rentPaymentFrequency;
    }

    public String getRentPayableInAdvance() {
        return rentPayableInAdvance;
    }

    public void setRentPayableInAdvance(String rentPayableInAdvance) {
        this.rentPayableInAdvance = rentPayableInAdvance;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public void setFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }

    public String getFirstPaymentAmount() {
        return firstPaymentAmount;
    }

    public void setFirstPaymentAmount(String firstPaymentAmount) {
        this.firstPaymentAmount = firstPaymentAmount;
    }

    public LocalDate getFirstPaymentPeriodEnd() {
        return firstPaymentPeriodEnd;
    }

    public void setFirstPaymentPeriodEnd(LocalDate firstPaymentPeriodEnd) {
        this.firstPaymentPeriodEnd = firstPaymentPeriodEnd;
    }

    public String getRentPaymentDayOrDate() {
        return rentPaymentDayOrDate;
    }

    public void setRentPaymentDayOrDate(String rentPaymentDayOrDate) {
        this.rentPaymentDayOrDate = rentPaymentDayOrDate;
    }

    public String getRentPaymentSchedule() {
        return rentPaymentSchedule;
    }

    public void setRentPaymentSchedule(String rentPaymentSchedule) {
        this.rentPaymentSchedule = rentPaymentSchedule;
    }

    public String getRentPaymentMethod() {
        return rentPaymentMethod;
    }

    public void setRentPaymentMethod(String rentPaymentMethod) {
        this.rentPaymentMethod = rentPaymentMethod;
    }

    public List<Service> getServicesIncludedInRent() {
        return servicesIncludedInRent;
    }

    public void setServicesIncludedInRent(List<Service> servicesIncludedInRent) {
        this.servicesIncludedInRent = servicesIncludedInRent;
    }

    public List<Service> getServicesProvidedByLettingAgent() {
        return servicesProvidedByLettingAgent;
    }

    public void setServicesProvidedByLettingAgent(List<Service> servicesProvidedByLettingAgent) {
        this.servicesProvidedByLettingAgent = servicesProvidedByLettingAgent;
    }

    public List<Service> getServicesLettingAgentIsFirstContactFor() {
        return servicesLettingAgentIsFirstContactFor;
    }

    public void setServicesLettingAgentIsFirstContactFor(List<Service> servicesLettingAgentIsFirstContactFor) {
        this.servicesLettingAgentIsFirstContactFor = servicesLettingAgentIsFirstContactFor;
    }

    public List<String> getIncludedAreasOrFacilities() {
        return includedAreasOrFacilities;
    }

    public void setIncludedAreasOrFacilities(List<String> includedAreasOrFacilities) {
        this.includedAreasOrFacilities = includedAreasOrFacilities;
    }

    public List<String> getExcludedAreasFacilities() {
        return excludedAreasFacilities;
    }

    public void setExcludedAreasFacilities(List<String> excludedAreasFacilities) {
        this.excludedAreasFacilities = excludedAreasFacilities;
    }

    public List<String> getSharedFacilities() {
        return sharedFacilities;
    }

    public void setSharedFacilities(List<String> sharedFacilities) {
        this.sharedFacilities = sharedFacilities;
    }

    public String getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(String depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getTenancyDepositSchemeAdministrator() {
        return tenancyDepositSchemeAdministrator;
    }

    public void setTenancyDepositSchemeAdministrator(String tenancyDepositSchemeAdministrator) {
        this.tenancyDepositSchemeAdministrator = tenancyDepositSchemeAdministrator;
    }

    public OptionalTerms getOptionalTerms() {
        return optionalTerms;
    }

    public void setOptionalTerms(OptionalTerms optionalTerms) {
        this.optionalTerms = optionalTerms;
    }

    public MustIncludeTerms getMustIncludeTerms() {
        return mustIncludeTerms;
    }

    public void setMustIncludeTerms(MustIncludeTerms mustIncludeTerms) {
        this.mustIncludeTerms = mustIncludeTerms;
    }

    public List<Term> getAdditionalTerms() {
        return additionalTerms;
    }

    public void setAdditionalTerms(List<Term> additionalTerms) {
        this.additionalTerms = additionalTerms;
    }

    public List<String> getExcludedTerms() {
        return excludedTerms;
    }

    public void setExcludedTerms(List<String> excludedTerms) {
        this.excludedTerms = excludedTerms;
    }

}
