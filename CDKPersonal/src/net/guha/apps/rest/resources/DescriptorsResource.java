package net.guha.apps.rest.resources;

import net.guha.apps.rest.DescriptorUtils;
import org.openscience.cdk.exception.CDKException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.net.MalformedURLException;


public class DescriptorsResource extends Resource {

    String smiles;

    public DescriptorsResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = (String) request.getAttributes().get("smiles");
        if (smiles != null) smiles = Reference.decode(smiles);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        try {
            String[] names = DescriptorUtils.getAvailableDescriptorNames("all");
            representation = new StringRepresentation(DescriptorUtils.namesToXml(names), MediaType.TEXT_XML);
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return representation;
    }

 




}