package space.foxmail.bingpicture;

import android.media.audiofx.Equalizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ContentHandler extends DefaultHandler {
    private String nodeName;
    private StringBuilder url;
    private StringBuilder copyright;
    private StringBuilder date;
    @Override
    public void startDocument() throws SAXException {
      url=new StringBuilder();
      copyright=new StringBuilder();
      date=new StringBuilder();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        nodeName=localName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if("urlBase".equals(nodeName)){
            url.append(ch,start,length);
        }
        if("copyright".equals(nodeName)){
            copyright.append(ch,start,length);
        }
        if("enddate".equals(nodeName)){
            date.append(ch,start,length);
        }
    }
    public String getUrl(){
        return url.toString();
    }
    public String getCopyright(){
        return copyright.toString();
    }
    public String getDate(){return date.toString();}
}
