package scot.mygov.housing.forms.modeltenancy;

import scot.mygov.housing.forms.AbstractDocumentGenerationResource;
import scot.mygov.housing.forms.DocumentGenerationService;
import scot.mygov.housing.forms.RecaptchaCheck;
import scot.mygov.housing.forms.modeltenancy.model.ModelTenancy;
import scot.mygov.validation.Validator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("model-tenancy")
public class ModelTenancyResource extends AbstractDocumentGenerationResource<ModelTenancy> {

    ModelTenancyJsonTemplateLoader jsonTemplateLoader;

    Validator<ModelTenancy> validator;

    @Inject
    public ModelTenancyResource(
            DocumentGenerationService<ModelTenancy> service,
            Validator<ModelTenancy> validator,
            RecaptchaCheck recaptchaCheck,
            ModelTenancyJsonTemplateLoader jsonTemplateLoader) {
        super(service, recaptchaCheck);
        this.validator = validator;
        this.jsonTemplateLoader = jsonTemplateLoader;
    }

    @GET
    @Path("template")
    @Produces(MediaType.APPLICATION_JSON)
    public ModelTenancy modelTenancyTemplate() throws ModelTenancyServiceException {
        try {
            return jsonTemplateLoader.loadJsonTemplate();
        } catch (RuntimeException e) {
            throw new ModelTenancyServiceException("Failed to load model tenancy template", e);
        }
    }

    @Override
    protected void validate(ModelTenancy model) {
        validator.validate(model);
    }

    protected String contentDispositionFilenameStem() {
        return "your-tenancy-agreement";
    }

    protected Class getModelClass() {
        return ModelTenancy.class;
    }
}
