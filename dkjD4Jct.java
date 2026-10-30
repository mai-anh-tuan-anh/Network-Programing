
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class dkjD4Jct {

    // ============================================================
    // 1. CONFIGURATION: Cau hinh Web Service
    // ============================================================
    static final String ENDPOINT =
            "http://36.50.135.242:2221/CharacterService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "dkjD4Jct";

    public static void main(String[] args) throws Exception {

        // ========================================================
        // 2. INPUT: Nhan danh sach ma ky tu tu server
        // ========================================================
        List<Integer> characters =
                requestCharacter(STUDENT_CODE, QUESTION_CODE);

        System.out.println("Input data: " + characters);

        // ========================================================
        // 3. ALGORITHM: Xoay vong phai theo ma ASCII ky tu dau tien
        // Collections.rotate(list, k) xoay phai k vi tri.
        // So lan xoay tuong duong voi ma ASCII cua phan tu dau tien.
        // ========================================================
        if (!characters.isEmpty()) {
            int rotations = characters.get(0);
            Collections.rotate(characters, rotations);
        }

        System.out.println("Result: " + characters);

        // ========================================================
        // 4. OUTPUT: Gui danh sach da xoay ve server
        // ========================================================
        submitCharacterCharArray(
                STUDENT_CODE, QUESTION_CODE, characters);

        System.out.println("Submission completed.");
    }

    // ============================================================
    // SOAP HELPER: Gui SOAP request va nhan XML response
    // Tai su dung cho cac operation cua Web Service.
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
            // Gui SOAP request den server.
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

            // Doc XML response va ngan XML external entity attacks.
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
    // INPUT: requestCharacter(studentCode, qCode) -> List<Integer>
    // Cac so nguyen bieu dien ma ky tu.
    // ============================================================
    static List<Integer> requestCharacter(
            String studentCode, String questionCode) throws Exception {

        String parametersXml =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(questionCode)
                + "</qCode>";

        Document response =
                callSoap("requestCharacter", parametersXml);

        NodeList returnNodes =
                response.getElementsByTagNameNS("*", "return");

        List<Integer> characters = new ArrayList<>();

        for (int i = 0; i < returnNodes.getLength(); i++) {
            String value =
                    returnNodes.item(i).getTextContent().trim();

            if (!value.isEmpty()) {
                characters.add(Integer.parseInt(value));
            }
        }

        return characters;
    }

    // ============================================================
    // OUTPUT: submitCharacterCharArray(studentCode, qCode, data)
    // Gui tung phan tu trong List<Integer> bang the <data>.
    // ============================================================
    static void submitCharacterCharArray(
            String studentCode,
            String questionCode,
            List<Integer> data) throws Exception {

        StringBuilder parametersXml = new StringBuilder();

        parametersXml.append(
                "<studentCode>").append(escapeXml(studentCode))
                .append("</studentCode>");

        parametersXml.append(
                "<qCode>").append(escapeXml(questionCode))
                .append("</qCode>");

        for (Integer character : data) {
            parametersXml.append("<data>")
                    .append(character)
                    .append("</data>");
        }

        callSoap(
                "submitCharacterCharArray",
                parametersXml.toString());
    }

    // ============================================================
    // UTILITY: Xu ly ky tu dac biet trong XML
    // ============================================================
    static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
