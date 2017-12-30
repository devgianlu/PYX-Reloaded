package net.socialgamer.cah.servlets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQLDialect;

import java.util.Map;

import static net.socialgamer.cah.Utils.methodNotAllowed;

public class SchemaResponder implements RouterNanoHTTPD.UriResponder { // FIXME: "Error: org.hibernate.HibernateException : /hibernate.cfg.xml not found"
    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        final Configuration c = new Configuration();
        c.configure();
        final String[] ls = c.generateSchemaCreationScript(new PostgreSQLDialect());
        StringBuilder builder = new StringBuilder();
        for (final String l : ls) builder.append(l).append(";");
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "", builder.toString());
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return methodNotAllowed();
    }
}
