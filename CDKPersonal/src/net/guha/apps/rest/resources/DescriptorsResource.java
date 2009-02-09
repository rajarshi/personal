package net.guha.apps.rest.resources;

import net.guha.apps.rest.Utils;
import nu.xom.Attribute;
import nu.xom.Element;
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
        try {
            String[] names = Utils.getAvailableDescriptorNames("all");
            if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
                StringBuffer result = new StringBuffer();
                for (String s : names) {
                    if (smiles == null) result.append(getHost() + s + "\n");
                    else result.append(getHost() + s + "/" + smiles + "\n");
                }
                result.deleteCharAt(result.length() - 1);
                representation = new StringRepresentation(result, MediaType.TEXT_PLAIN);
            } else
                representation = new StringRepresentation(namesToXml(names, smiles), MediaType.TEXT_XML);
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return representation;
    }

    private String getHost() {
        return "http://" + getRequest().getHostRef().getHostDomain() + ":" +
                getRequest().getHostRef().getHostPort() + "/";
    }

    public String namesToXml(String[] names, String extra) {
        String elemContainer = "specification-list";
	String elemName = "specification-ref";
        if (extra == null) extra = "";
        else {
		extra = "/" + extra;
		elemContainer = "descriptor-list";
		elemName = "descriptor-ref";
	}
        Element root = new Element(elemContainer);
        for (String s : names) {
            Element element = new Element(elemName);
            element.addAttribute(new Attribute("href", getHost() + "cdk/descriptor/" + s + extra));
            root.appendChild(element);
        }
        return root.toXML();
    }


}
