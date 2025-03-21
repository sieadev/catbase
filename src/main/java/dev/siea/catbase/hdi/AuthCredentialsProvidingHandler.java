package dev.siea.catbase.hdi;

import com.pixelservices.flash.components.ExpectedRequestParameter;
import com.pixelservices.flash.components.RequestHandler;
import com.pixelservices.flash.lifecycle.Request;
import com.pixelservices.flash.lifecycle.Response;
import com.pixelservices.flash.shaded.org.json.JSONObject;

/**
 * Bottom-level HDI that ensures authentication for handlers that require it, and
 * provides the email and password for the authenticated user.
 */
public abstract class AuthCredentialsProvidingHandler extends RequestHandler {
    protected ExpectedRequestParameter emailField;
    protected String email;
    protected String password;

    public AuthCredentialsProvidingHandler(Request req, Response res) {
        super(req, res);
        emailField = expectedRequestParameter("email", "The email to register with");
    }

    @Override
    public Object handle() {
        this.email = emailField.getString();
        String workingPwd = req.header("Authorization");

        if (workingPwd == null || !workingPwd.startsWith("Bearer ")) {
            res.status(401);
            return new JSONObject().put("error", "Missing or invalid Authorization header").toString();
        }

        this.password = workingPwd.replace("Bearer ", "").trim();

        return handleProvidedCredentials();
    }

    protected abstract Object handleProvidedCredentials();
}
