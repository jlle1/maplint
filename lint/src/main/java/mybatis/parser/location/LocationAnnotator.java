package mybatis.parser.location
        ;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Stack;

public class LocationAnnotator extends XMLFilterImpl {

    private Locator locator;
    private Element lastAddedElement;
    private Stack<Locator> locatorStack = new Stack<Locator>();
    private UserDataHandler dataHandler = new LocationDataHandler();

    public LocationAnnotator(XMLReader xmlReader, Document dom) {
        super(xmlReader);

        // Add listener to DOM, so we know which node was added.
        EventListener modListener = new EventListener() {
            @Override
            public void handleEvent(Event e) {
                EventTarget target = ((MutationEvent) e).getTarget();
                lastAddedElement = (Element) target;
            }
        };
        ((EventTarget) dom).addEventListener("DOMNodeInserted",
                modListener, true);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);

        // Keep snapshot of start location,
        // for later when end of element is found.
        locatorStack.push(new LocatorImpl(locator));
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        // Mutation event fired by the adding of element end,
        // and so lastAddedElement will be set.
        super.endElement(uri, localName, qName);

        if (locatorStack.size() > 0) {
            Locator startLocator = locatorStack.pop();

            LocationData location = new LocationData(
                    startLocator.getSystemId(),
                    startLocator.getLineNumber(),
                    startLocator.getColumnNumber(),
                    locator.getLineNumber(),
                    locator.getColumnNumber());

            lastAddedElement.setUserData(
                    LocationData.LOCATION_DATA_KEY, location,
                    dataHandler);
        }
    }

    // Ensure location data copied to any new DOM node.
    private class LocationDataHandler implements UserDataHandler {

        @Override
        public void handle(short operation, String key, Object data,
                           Node src, Node dst) {

            if (src != null && dst != null) {
                LocationData locatonData = (LocationData)
                        src.getUserData(LocationData.LOCATION_DATA_KEY);

                if (locatonData != null) {
                    dst.setUserData(LocationData.LOCATION_DATA_KEY,
                            locatonData, dataHandler);
                }
            }
        }
    }
}