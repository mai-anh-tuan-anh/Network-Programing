
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class q6StSteFp {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/ObjectService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "6StSteFp";

    public static void main(String[] args) {
        try {
            // 1. Nhan ProductY tu server
            ProductY product = requestProductY(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input: " + product);

            // 2. Tinh finalPrice
            product.finalPrice = product.price
                    * (1 + product.taxRate / 100.0)
                    * (1 - product.discount / 100.0);

            System.out.println("Final Price: "
                    + product.finalPrice);

            // 3. Gui ProductY ve server
            String status = submitProductY(
                    STUDENT_CODE, QUESTION_CODE, product);

            System.out.println("Status: " + status);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // DATA MODEL: ProductY
    // =============================================
    static class ProductY {
        String name;
        double price;
        double taxRate;
        double discount;
        double finalPrice;

        @Override
        public String toString() {
            return "ProductY{name=" + name
                    + ", price=" + price
                    + ", taxRate=" + taxRate
                    + ", discount=" + discount
                    + ", finalPrice=" + finalPrice + "}";
        }
    }

    // =============================================
    // SOAP HELPER
    // =============================================
    static Document callSoap(
            String operation, String parametersXml)
            throws Exception {

        String requestXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope"
                + " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:tns=\"" + NAMESPACE + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<tns:" + operation + ">"
                + parametersXml
                + "</tns:" + operation + ">"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";

        HttpURLConnection conn = (HttpURLConnection)
                new URL(ENDPOINT).openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            conn.setRequestProperty(
                    "Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("SOAPAction", "\"\"");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestXml.getBytes(
                        StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (stream == null) {
                throw new IOException(
                        "HTTP status: " + status);
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);

            Document response;

            try (InputStream input = stream) {
                response = factory.newDocumentBuilder()
                        .parse(input);
            }

            NodeList faults =
                    response.getElementsByTagNameNS("*", "Fault");

            if (faults.getLength() > 0) {
                throw new IOException(
                        "SOAP Fault: "
                        + faults.item(0).getTextContent());
            }

            if (status < 200 || status >= 300) {
                throw new IOException(
                        "HTTP status: " + status);
            }

            return response;

        } finally {
            conn.disconnect();
        }
    }

    // =============================================
    // XML HELPER
    // =============================================
    static String getChildText(
            Element parent, String name) {

        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node instanceof Element) {
                Element element = (Element) node;

                String localName = element.getLocalName();

                if (localName == null) {
                    localName = element.getNodeName();

                    int colon = localName.indexOf(':');
                    if (colon >= 0) {
                        localName = localName.substring(colon + 1);
                    }
                }

                if (name.equals(localName)) {
                    return element.getTextContent().trim();
                }
            }
        }

        return null;
    }

    // =============================================
    // INPUT: requestProductY
    // =============================================
    static ProductY requestProductY(
            String studentCode,
            String qCode) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response =
                callSoap("requestProductY", params);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            throw new IOException(
                    "Server khong tra ve ProductY");
        }

        Element element = (Element) elements.item(0);

        ProductY product = new ProductY();

        product.name = getChildText(element, "name");

        product.price = Double.parseDouble(
                getChildText(element, "price"));

        product.taxRate = Double.parseDouble(
                getChildText(element, "taxRate"));

        product.discount = Double.parseDouble(
                getChildText(element, "discount"));

        String finalPrice =
                getChildText(element, "finalPrice");

        product.finalPrice = finalPrice == null
                ? 0.0
                : Double.parseDouble(finalPrice);

        return product;
    }

    // =============================================
    // OUTPUT: submitProductY
    // =============================================
    static String submitProductY(
            String studentCode,
            String qCode,
            ProductY product) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>"
                + "<data>"
                + "<name>" + escapeXml(product.name)
                + "</name>"
                + "<price>" + product.price
                + "</price>"
                + "<taxRate>" + product.taxRate
                + "</taxRate>"
                + "<discount>" + product.discount
                + "</discount>"
                + "<finalPrice>" + product.finalPrice
                + "</finalPrice>"
                + "</data>";

        Document response =
                callSoap("submitProductY", params);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            return "No status returned";
        }

        return elements.item(0)
                .getTextContent().trim();
    }

    // =============================================
    // XML ESCAPE
    // =============================================
    static String escapeXml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
