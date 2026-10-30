
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class rgiiqtqm {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/ObjectService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "rgiiqtqm";

    static class Customer {
        String customerId;
        String location;
        int purchaseCount;
        float totalSpent;

        Customer(String customerId, String location,
                 int purchaseCount, float totalSpent) {
            this.customerId = customerId;
            this.location = location;
            this.purchaseCount = purchaseCount;
            this.totalSpent = totalSpent;
        }
    }

    public static void main(String[] args) throws Exception {
        List<Customer> customers =
                requestListCustomer(STUDENT_CODE, QUESTION_CODE);

        List<Customer> result = new ArrayList<>();

        for (Customer customer : customers) {
    if (customer.totalSpent > 500.0f
            && customer.purchaseCount >= 3) {
        result.add(customer);
    }
}

        System.out.println("Input customer count: " + customers.size());
        System.out.println("Qualified customer count: " + result.size());

        for (Customer customer : result) {
            System.out.println(
                    customer.customerId + ";"
                    + customer.location + ";"
                    + customer.purchaseCount + ";"
                    + customer.totalSpent
            );
        }

        submitListCustomer(STUDENT_CODE, QUESTION_CODE, result);

        System.out.println("Submission completed.");
    }

    static Document callSoap(
            String operation, String parametersXml) throws Exception {

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

        HttpURLConnection connection =
                (HttpURLConnection) new URL(ENDPOINT).openConnection();

        connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setDoOutput(true);

        connection.setRequestProperty(
                "Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "\"\"");

        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestXml.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();

            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                throw new IOException("HTTP status: " + statusCode);
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
                response = factory.newDocumentBuilder().parse(input);
            }

            NodeList faults =
                    response.getElementsByTagNameNS("*", "Fault");

            if (faults.getLength() > 0) {
                throw new IOException(
                        "SOAP Fault: " + faults.item(0).getTextContent());
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("HTTP status: " + statusCode);
            }

            return response;
        } finally {
            connection.disconnect();
        }
    }

    static String getChildText(Element parent, String wantedName) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;
            String localName = element.getLocalName();

            if (localName == null) {
                localName = element.getNodeName();
                int colon = localName.indexOf(':');

                if (colon >= 0) {
                    localName = localName.substring(colon + 1);
                }
            }

            if (wantedName.equals(localName)) {
                return element.getTextContent().trim();
            }
        }

        return null;
    }

    static List<Customer> requestListCustomer(
            String studentCode, String qCode) throws Exception {

        String parametersXml =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        Document response =
                callSoap("requestListCustomer", parametersXml);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);

            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;

            String customerId = getChildText(element, "customerId");
            String location = getChildText(element, "location");
            String purchaseCountText =
                    getChildText(element, "purchaseCount");
            String totalSpentText =
                    getChildText(element, "totalSpent");

            if (customerId != null && location != null
                    && purchaseCountText != null
                    && totalSpentText != null) {

                customers.add(new Customer(
                        customerId,
                        location,
                        Integer.parseInt(purchaseCountText),
                        Float.parseFloat(totalSpentText)
                ));
            }
        }

        return customers;
    }

    static void submitListCustomer(
            String studentCode, String qCode,
            List<Customer> customers) throws Exception {

        StringBuilder parametersXml = new StringBuilder();

        parametersXml.append("<studentCode>")
                .append(escapeXml(studentCode))
                .append("</studentCode>");

        parametersXml.append("<qCode>")
                .append(escapeXml(qCode))
                .append("</qCode>");

        for (Customer customer : customers) {
            parametersXml.append("<data>")
                    .append("<customerId>")
                    .append(escapeXml(customer.customerId))
                    .append("</customerId>")
                    .append("<location>")
                    .append(escapeXml(customer.location))
                    .append("</location>")
                    .append("<purchaseCount>")
                    .append(customer.purchaseCount)
                    .append("</purchaseCount>")
                    .append("<totalSpent>")
                    .append(Float.toString(customer.totalSpent))
                    .append("</totalSpent>")
                    .append("</data>");
        }

        callSoap("submitListCustomer", parametersXml.toString());
    }

    static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
