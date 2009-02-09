package net.guha.apps.rest.resources;

import net.guha.apps.rest.DescriptorUtils;
import net.guha.apps.rest.Utils;
import org.openscience.cdk.exception.CDKException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.IOException;
import java.net.MalformedURLException;


public class DescriptorResource extends Resource {
    String klass = "";
    String smiles;

    public DescriptorResource(Context context, Request request, Response response) {
        super(context, request, response);
        klass = (String) request.getAttributes().get("klass");
        if (klass != null) klass = Reference.decode(klass);
        smiles = (String) request.getAttributes().get("smiles");
        if (smiles != null) smiles = Reference.decode(smiles);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        if (klass == null)
            throw new ResourceException(new CDKException("Must provide a descriptor class and/or SMILES"));
        try {
            if (klass.startsWith("org") && smiles == null) {
                result = Utils.getDescriptorSpecifications(new String[]{klass});
                representation = new StringRepresentation(result, MediaType.TEXT_XML);
            } else if (klass.startsWith("org") && smiles != null) {
                result = Utils.evaluateDescriptors(new String[]{klass}, smiles);
                representation = new StringRepresentation(result, MediaType.TEXT_XML);
            }
        } catch (MalformedURLException e) {
            throw new ResourceException(new CDKException("Must provide a descriptor class or SMILES"));
        } catch (CDKException e) {
            throw new ResourceException(new CDKException("Must provide a descriptor class or SMILES"));
        } catch (ClassNotFoundException e) {
            throw new ResourceException(new CDKException("Invalid descriptor class"));
        } catch (IllegalAccessException e) {
            throw new ResourceException(new CDKException("Must provide a descriptor class or SMILES"));
        } catch (InstantiationException e) {
            throw new ResourceException(new CDKException("Error loading descriptor class"));
        } catch (IOException e) {
            throw new ResourceException(new CDKException("Error loading descriptor dictionary"));
        }
        return representation;
    }
}