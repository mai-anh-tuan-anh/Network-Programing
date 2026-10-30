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
import org.w3c.dom.NodeList;

public class WlJ4tCwZ {

    // ============================================================
    // 1. CONFIGURATION: Cau hinh Web Service
    // ============================================================
    static final String ENDPOINT =
            "http://36.50.135.242:2221/DataService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "WlJ4tCwZ";

    public static void main(String[] args) throws Exception {

        // ========================================================
        // 2. INPUT: Lay danh sach so nguyen tu server
        // ========================================================
        List<Integer> numbers = getData(STUDENT_CODE, QUESTION_CODE);

        System.out.println("Input data: " + numbers);

        // ========================================================
        // 3. ALGORITHM: Tinh tong cac phan tu
        // Sua phan nay theo yeu cau thuat toan cua tung bai.
        // ========================================================
        int sum = 0;

        for (Integer number : numbers) {
            sum += number;
        }

        System.out.println("Sum: " + sum);

        // ========================================================
        // 4. OUTPUT: Gui tong ve server bang submitDataInt
        // ========================================================
        submitDataInt(STUDENT_CODE, QUESTION_CODE, sum);

        System.out.println("Submission completed.");
    }

    // ============================================================
    // SOAP HELPER: Gui SOAP request va nhan XML response
    // Dung lai ham nay cho cac bai SOAP cung kieu.
    // ============================================================
    static Document callSoap(String operation, String parametersXml)
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
            // Gui request den Web Service.
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestXml.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();

            InputStream responseStream =
                    (statusCode >= 200 && statusCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (responseStream == null) {
                throw new IOException(
                        "No response body. HTTP status: " + statusCode);
            }

            // Parse XML response va vo hieu hoa external entities.
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

            try (InputStream input = responseStream) {
                response = factory.newDocumentBuilder().parse(input);
            }

            // Kiem tra SOAP Fault.
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

    // ============================================================
    // INPUT: getData(studentCode, qCode) -> List<Integer>
    // ============================================================
    static List<Integer> getData(
            String studentCode, String questionCode) throws Exception {

        String parametersXml =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(questionCode)
                + "</qCode>";

        Document response = callSoap("getData", parametersXml);

        NodeList returnNodes =
                response.getElementsByTagNameNS("*", "return");

        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < returnNodes.getLength(); i++) {
            String value =
                    returnNodes.item(i).getTextContent().trim();

            if (!value.isEmpty()) {
                numbers.add(Integer.parseInt(value));
            }
        }

        return numbers;
    }

    // ============================================================
    // OUTPUT: submitDataInt(studentCode, qCode, data)
    // ============================================================
    static void submitDataInt(
            String studentCode,
            String questionCode,
            int data) throws Exception {

        String parametersXml =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(questionCode)
                + "</qCode>"
                + "<data>" + data + "</data>";

        callSoap("submitDataInt", parametersXml);
    }

    // ============================================================
    // UTILITY: Escape cac ky tu dac biet trong XML
    // ============================================================
    static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}