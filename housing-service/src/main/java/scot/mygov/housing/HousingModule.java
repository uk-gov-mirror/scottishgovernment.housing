package scot.mygov.housing;

import com.codahale.metrics.MetricRegistry;
import dagger.Module;
import dagger.Provides;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.mygov.config.Configuration;
import scot.mygov.documents.DateSwitchingDocumentTemplateLoader;
import scot.mygov.documents.DocumentGenerator;
import scot.mygov.documents.DocumentTemplateLoader;
import scot.mygov.documents.DocumentTemplateLoaderBasicImpl;
import scot.mygov.housing.cpi.CPIService;
import scot.mygov.housing.europa.Europa;
import scot.mygov.housing.forms.DocumentGenerationService;
import scot.mygov.housing.forms.PlaceholderProvidingMergingCallback;
import scot.mygov.housing.forms.RecaptchaCheck;
import scot.mygov.housing.forms.foreigntraveldeclaration.ForeignTravelDeclarationFieldExtractor;
import scot.mygov.housing.forms.foreigntraveldeclaration.model.ForeignTravelDeclaration;
import scot.mygov.housing.forms.modeltenancy.ModelTenancyFieldExtractor;
import scot.mygov.housing.forms.modeltenancy.ModelTenancyMergingCallback;
import scot.mygov.housing.forms.modeltenancy.model.ModelTenancy;
import scot.mygov.housing.forms.modeltenancy.validation.ModelTenancyValidatorFactory;
import scot.mygov.housing.forms.nonprovisionofdocumentation.NonProvisionOfDocumentationFieldExtractor;
import scot.mygov.housing.forms.nonprovisionofdocumentation.model.NonProvisionOfDocumentation;
import scot.mygov.housing.forms.noticetoleave.NoticeToLeaveFieldExtractor;
import scot.mygov.housing.forms.noticetoleave.NoticeToLeavePlaceholders;
import scot.mygov.housing.forms.noticetoleave.model.NoticeToLeave;
import scot.mygov.housing.forms.rentadjudication.RentAdjudicationFieldExtractor;
import scot.mygov.housing.forms.rentadjudication.model.RentAdjudication;
import scot.mygov.housing.forms.rentincreaseforimprovementsnotice.RentIncreaseForImprovementsFieldExtractor;
import scot.mygov.housing.forms.rentincreaseforimprovementsnotice.RentIncreaseForImprovementsPlaceholders;
import scot.mygov.housing.forms.rentincreaseforimprovementsnotice.model.RentIncreaseForImprovements;
import scot.mygov.housing.forms.rentincreasenotice.RentIncreaseFieldExtractor;
import scot.mygov.housing.forms.rentincreasenotice.RentIncreaseRPZSectionRemovingCallback;
import scot.mygov.housing.forms.rentincreasenotice.model.RentIncrease;
import scot.mygov.housing.postcode.EuropaPostcodeService;
import scot.mygov.housing.postcode.PostcodeService;
import scot.mygov.housing.rpz.RPZService;
import scot.mygov.housing.rpz.StubRPZService;
import scot.mygov.validation.Validator;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Module
public class HousingModule {

    private static final Logger LOG = LoggerFactory.getLogger(HousingConfiguration.class);

    public static final String EUROPA_TARGET = "europaTarget";
    public static final String FAIR_RENT_TARGET = "fairRentTarget";

    public static final String STANDARD_CLIENT = "standardClient";
    public static final String FAIR_RENT_CLIENT = "fairRentClient";

    private static final String APP_NAME = "housing";

    @Provides
    @Singleton
    HousingConfiguration configuration() {
        Configuration<HousingConfiguration> configuration = Configuration
                .load(new HousingConfiguration(), APP_NAME)
                .validate();
        LOG.info("{}", configuration);
        return configuration.getConfiguration();
    }

    @Provides
    @Named(EUROPA_TARGET)
    WebTarget europaTarget(@Named(STANDARD_CLIENT) Client client, HousingConfiguration configuration) {
        String path = String.format("/%s/os/abpr/address", configuration.getEuropaId());
        return client.target(configuration.getEuropaURI()).path(path);
    }

    @Provides
    @Named(FAIR_RENT_TARGET)
    WebTarget fairRentTarget(@Named(FAIR_RENT_CLIENT) Client client, HousingConfiguration configuration) {
        HousingConfiguration.FairRentRegister fairRentConfig = configuration.getFairRentRegister();
        return client.target(fairRentConfig.getUri());
    }

    @Provides
    @Named(STANDARD_CLIENT)
    @Singleton
    Client client() {
        return ((ResteasyClientBuilder) ClientBuilder.newBuilder())
                .connectionPoolSize(10)
                .connectionTTL(1, MINUTES)
                .connectTimeout(5, SECONDS)
                .readTimeout(10, SECONDS)
                .build();
    }

    @Provides
    @Named(FAIR_RENT_CLIENT)
    @Singleton
    Client fairRentClient(HousingConfiguration config) {
        int connectTimeout = config.getFairRentRegister().getConnectTimeoutSeconds();
        int readTimeout = config.getFairRentRegister().getReadTimeoutSeconds();
        ResteasyClientBuilder builder =
                (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()
                        .connectTimeout(connectTimeout, SECONDS)
                        .readTimeout(readTimeout, SECONDS);
        builder.disableTrustManager();
        Client client = builder.connectionPoolSize(10).build();
        String username = config.getFairRentRegister().getUsername();
        String password = config.getFairRentRegister().getPassword();
        ClientRequestFilter basicAuthFilter = new BasicAuthentication(username, password);
        client.register(basicAuthFilter);
        return client;
    }

    @Provides
    RPZService rpzService() {
        return new StubRPZService();
    }

    @Provides
    @Singleton
    AsposeLicense asposeLicense(HousingConfiguration configuration) {
        return new AsposeLicense(configuration.getAspose().getLicense());
    }

    @Provides
    @Singleton
    CPIService cpiService(HousingConfiguration configuration) {
        URI value = configuration.getCpi().getUrl();
        try {
            return new CPIService(value.toURL());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed to load CPI data from url" + value, e);
        }
    }

    @Provides
    @Singleton
    Europa europa(MetricRegistry registry, @Named(EUROPA_TARGET) WebTarget europaTarget) {
        return new Europa(europaTarget, registry);
    }


    @Provides
    PostcodeService postcodeService(Europa europa) {
        return new EuropaPostcodeService(europa);
    }

    @Provides
    @Singleton
    MetricRegistry metricsRegistry() {
        return new MetricRegistry();
    }

    @Provides
    @Singleton
    DocumentGenerationService<ModelTenancy> modelTenancyDocumentGenerationService(
            HousingConfiguration config,
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DateSwitchingDocumentTemplateLoader templateLoader = new DateSwitchingDocumentTemplateLoader();
        LocalDate legislationChangeDate2024 = LocalDate.parse(config.getLegislationChangeDate2024());

        templateLoader.addDocument(
            LocalDate.of(2010, 1, 1),
            new DocumentTemplateLoaderBasicImpl("/templates/model-tenancy-agreement.docx", asposeLicense));
        templateLoader.addDocument(
            legislationChangeDate2024,
            new DocumentTemplateLoaderBasicImpl("/templates/model-tenancy-agreement-2024.docx", asposeLicense));

        return  new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader),
                new ModelTenancyFieldExtractor(),
                ModelTenancyMergingCallback::new,
                metricRegistry);
    }

    @Provides
    DocumentGenerationService<RentAdjudication> rentAdjudicationDocumentGenerationService(
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DocumentTemplateLoader templateLoader
                = new DocumentTemplateLoaderBasicImpl("/templates/rent-adjudication.docx", asposeLicense);
        return new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader), new RentAdjudicationFieldExtractor(),metricRegistry);
    }

    @Provides
    DocumentGenerationService<ForeignTravelDeclaration> foreignTravelDeclarationDocumentGenerationService(
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DocumentTemplateLoader templateLoader
                = new DocumentTemplateLoaderBasicImpl("/templates/travel-declaration.docx", asposeLicense);
        return new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader), new ForeignTravelDeclarationFieldExtractor(), metricRegistry);
    }

    @Provides
    DocumentGenerationService<RentIncrease> rentIncreaseDocumentGenerationService(
            HousingConfiguration config,
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DateSwitchingDocumentTemplateLoader templateLoader = new DateSwitchingDocumentTemplateLoader();
        LocalDate legislationChangeDate2023 = LocalDate.parse(config.getLegislationChangeDate2023());
        LocalDate legislationChangeDate2024 = LocalDate.parse(config.getLegislationChangeDate2024());

        templateLoader.addDocument(
            LocalDate.of(2010, 1, 1),
            new DocumentTemplateLoaderBasicImpl("/templates/rent-increase.docx", asposeLicense));
        templateLoader.addDocument(
            legislationChangeDate2023,
            new DocumentTemplateLoaderBasicImpl("/templates/rent-increase-2023.docx", asposeLicense));
        templateLoader.addDocument(
            legislationChangeDate2024,
            new DocumentTemplateLoaderBasicImpl("/templates/rent-increase-2024.docx", asposeLicense));

        return  new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader),
                new RentIncreaseFieldExtractor(),
                form -> new RentIncreaseRPZSectionRemovingCallback(),
                metricRegistry);
    }

    @Provides
    DocumentGenerationService<RentIncreaseForImprovements> rentIncreaseForImprovementsDocumentGenerationService(
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DocumentTemplateLoader templateLoader
                = new DocumentTemplateLoaderBasicImpl("/templates/rent-increase-for-improvements.docx", asposeLicense);
        return  new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader),
                new RentIncreaseForImprovementsFieldExtractor(),
                form -> new PlaceholderProvidingMergingCallback(RentIncreaseForImprovementsPlaceholders.placeholders()),
                metricRegistry);
    }

    @Provides
    DocumentGenerationService<NonProvisionOfDocumentation> nonProvisionOfDocumentationDocumentGenerationService(
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DocumentTemplateLoader templateLoader
                = new DocumentTemplateLoaderBasicImpl("/templates/non-provision-of-documentation.docx", asposeLicense);
        return  new DocumentGenerationService<>(
                    new DocumentGenerator(templateLoader),
                    new NonProvisionOfDocumentationFieldExtractor(),
                    metricRegistry);
    }

    @Provides
    DocumentGenerationService<NoticeToLeave> noticeToLeaveDocumentGenerationService(
            HousingConfiguration config,
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DateSwitchingDocumentTemplateLoader templateLoader = new DateSwitchingDocumentTemplateLoader();
        LocalDate legislationChangeDate2024 = LocalDate.parse(config.getLegislationChangeDate2024());

        templateLoader.addDocument(
            LocalDate.of(2010, 1, 1),
            new DocumentTemplateLoaderBasicImpl("/templates/notice-to-leave.docx", asposeLicense));
        templateLoader.addDocument(
            legislationChangeDate2024,
            new DocumentTemplateLoaderBasicImpl("/templates/notice-to-leave-2024.docx", asposeLicense));

        return  new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader),
                new NoticeToLeaveFieldExtractor(),
                form -> new PlaceholderProvidingMergingCallback(NoticeToLeavePlaceholders.placeholders()),
                metricRegistry);
    }

    @Named("subtenantNoticeToLeaveDocumentGenerationService")
    @Provides
    DocumentGenerationService<NoticeToLeave> subtenantNoticeToLeaveDocumentGenerationService(
            HousingConfiguration config,
            AsposeLicense asposeLicense,
            MetricRegistry metricRegistry) {

        DateSwitchingDocumentTemplateLoader templateLoader = new DateSwitchingDocumentTemplateLoader();
        LocalDate legislationChangeDate2024 = LocalDate.parse(config.getLegislationChangeDate2024());

        templateLoader.addDocument(
            LocalDate.of(2010, 1, 1),
            new DocumentTemplateLoaderBasicImpl("/templates/subtenant-notice-to-leave.docx", asposeLicense));
        templateLoader.addDocument(
            legislationChangeDate2024,
            new DocumentTemplateLoaderBasicImpl("/templates/subtenant-notice-to-leave-2024.docx", asposeLicense));

        return  new DocumentGenerationService<>(
                new DocumentGenerator(templateLoader),
                new NoticeToLeaveFieldExtractor(),
                form -> new PlaceholderProvidingMergingCallback(NoticeToLeavePlaceholders.placeholders()),
                metricRegistry);
    }

    @Provides
    RecaptchaCheck recaptchaCheck(HousingConfiguration configuration, @Named(STANDARD_CLIENT) Client client) {
        HousingConfiguration.Recaptcha recaptchaConfig = configuration.getRecaptcha();
        WebTarget verifyTarget = client.target(HousingConfiguration.Recaptcha.RECAPTCHA_VERIFY_URL);
        return new RecaptchaCheck(
                recaptchaConfig.isEnabled(),
                verifyTarget,
                recaptchaConfig.getSecret());
    }

    @Provides
    Validator<ModelTenancy> modelTenancyValidator() {
        return new ModelTenancyValidatorFactory().validator(false);
    }

    @Provides
    @Singleton
    @Nullable
    S3Client s3(HousingConfiguration configuration) {
        String region = configuration.getRegion();
        if (region == null) {
            return null;
        }
        return S3Client.builder()
            .region(Region.of(region))
            .build();
    }

}
