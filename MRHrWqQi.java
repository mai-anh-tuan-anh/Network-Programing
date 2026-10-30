
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class MRHrWqQi {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/CharacterService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "MRHrWqQi";

    public static void main(String[] args) {
        try {
            // 1. Nhan chuoi tu server
            String input = requestString(
                    STUDENT_CODE, QUESTION_CODE);

            if (input == null) {
                System.out.println("Server tra ve NULL!");
                return;
            }

            System.out.println("Input: " + input);

            // 2. Dao nguoc chuoi
            String reversed = new StringBuilder(input)
                    .reverse().toString();

            System.out.println("Output: " + reversed);

            // 3. Gui ket qua va nhan trang thai
            String status = submitString(
                    STUDENT_CODE, QUESTION_CODE, reversed);

            System.out.println("Status: " + status);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // SOAP HELPER
    // ==================================================
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

            int statusCode = conn.getResponseCode();

            InputStream stream =
                    statusCode >= 200 && statusCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (stream == null) {
                throw new IOException(
                        "HTTP status: " + statusCode);
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
                throw new IOException("SOAP Fault: "
                        + faults.item(0).getTextContent());
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException(
                        "HTTP status: " + statusCode);
            }

            return response;

        } finally {
            conn.disconnect();
        }
    }

    // ==================================================
    // INPUT: requestString(studentCode, qCode)
    // ==================================================
    static String requestString(
            String studentCode, String qCode)
            throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response = callSoap("requestString", params);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            return null;
        }

        return elements.item(0).getTextContent();
    }

    // ==================================================
    // OUTPUT: submitString(studentCode, qCode, data)
    // ==================================================
    static String submitString(
            String studentCode,
            String qCode,
            String reversedString) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>"
                + "<data>" + escapeXml(reversedString)
                + "</data>";

        Document response = callSoap("submitString", params);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            return "No status returned";
        }

        return elements.item(0).getTextContent().trim();
    }

    // ==================================================
    // XML ESCAPE
    // ==================================================
    static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
