package org.hibridgehie.validator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.json.JSONObject;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.mdht.uml.cda.util.CDAUtil;
import org.eclipse.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.cda.mu2consol.Mu2consolPackage;


@Path("/validate")
public class ValidatorService {

  public static synchronized JSONObject validateHelper(ByteArrayInputStream inStream)
      throws Exception {

    JSONObject ret = new JSONObject();

    ArrayList<String> errors = new ArrayList<String>();
    ArrayList<String> warnings = new ArrayList<String>();
    ArrayList<String> infos = new ArrayList<String>();

    ValidationResult result = new ValidationResult();

    long t0 = System.currentTimeMillis();

    CDAUtil.loadAs(
        inStream,
        Mu2consolPackage.eINSTANCE.getTransitionOfCareAmbulatorySummary(),
        result);

    for (Diagnostic diagnostic : result.getErrorDiagnostics()) {
      errors.add(diagnostic.getMessage());
    }
    for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
      warnings.add(diagnostic.getMessage());
    }
    for (Diagnostic diagnostic : result.getInfoDiagnostics()) {
      infos.add(diagnostic.getMessage());
    }
    long t1 = System.currentTimeMillis();

    ret.element("schemaValidationDiagnostics", result.getSchemaValidationDiagnostics().size());
    ret.element("emfResourceDiagnostics", result.getEMFResourceDiagnostics().size());
    ret.element("emfValidationDiagnostics", result.getEMFValidationDiagnostics().size());
    ret.element("totalDiagnostics", result.getAllDiagnostics().size());

    ret.element("valid", !result.hasErrors());
    ret.element("errors", errors);
    ret.element("warnings", warnings);
    ret.element("infos", infos);
    ret.element("processingTimeMs", (t1 - t0));

    if (!result.hasErrors()) {
      System.out.println("Document is valid");
    } else {
      System.out.println("Document is invalid");
    }

    return ret;
  }

  @POST
  @Consumes({ "application/xml", "*/*" })
  @Produces({ MediaType.APPLICATION_JSON })
  public String validateCcda(String docXml) throws Exception {
    ByteArrayInputStream inStream = null;

    try {
      inStream = new ByteArrayInputStream(docXml.getBytes("UTF-8"));
    } catch (IOException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    return validateHelper(inStream).toString(2);
  }
}
