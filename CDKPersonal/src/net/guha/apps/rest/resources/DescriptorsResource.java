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

import nu.xom.Element;
import nu.xom.Attribute;


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
        try {
            String[] names = DescriptorUtils.getAvailableDescriptorNames("all");
            if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
                StringBuffer result = new StringBuffer();
                for (String s : names) result.append(s + "\n");
                result.deleteCharAt(result.length()-1);
                representation = new StringRepresentation(result, MediaType.TEXT_PLAIN);
            } else
                representation = new StringRepresentation(namesToXml(names), MediaType.TEXT_XML);
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return representation;
    }

    public String namesToXml(String[] names) {
        String host = "http://" + getRequest().getHostRef().getHostDomain() + ":" +
                getRequest().getHostRef().getHostPort() + "/";
        Element root = new Element("descriptor-list");
        for (String s : names) {
            Element element = new Element("descriptor-ref");
            element.addAttribute(new Attribute("href", host + "cdk/descriptor/" + s));
            root.appendChild(element);
        }
        return root.toXML();
    }


}