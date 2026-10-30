
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class as9JBcfr {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/CharacterService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "as9JBcfr";

    public static void main(String[] args) {
        try {
            List<String> words = requestStringArray(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input: " + words);

            // Nhom theo so nguyen am tang dan
            Map<Integer, List<String>> groups = new TreeMap<>();

            for (String word : words) {
                int count = countVowels(word);

                groups.computeIfAbsent(
                        count, k -> new ArrayList<>()
                ).add(word);
            }

            List<String> result = new ArrayList<>();

            for (Map.Entry<Integer, List<String>> entry
                    : groups.entrySet()) {

                List<String> group = entry.getValue();

                // Sap xep tu dien
                Collections.sort(group);

                // Noi bang dau phay, khong them space
                String groupString = String.join(",", group);

                result.add(groupString);

                System.out.println(
                        "Vowels = " + entry.getKey()
                        + " | " + groupString);
            }

            System.out.println("Output: " + result);

            submitCharacterStringArray(
                    STUDENT_CODE, QUESTION_CODE, result);

            System.out.println("Submission completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // DEM NGUYEN AM
    // ==========================================
    static int countVowels(String word) {
        int count = 0;
        String lower = word.toLowerCase(Locale.ROOT);

        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);

            if (c == 'a' || c == 'e' || c == 'i'
                    || c == 'o' || c == 'u') {
                count++;
            }
        }
        return count;
    }

    // ==========================================
    // SOAP HELPER
    // ==========================================
    static Document callSoap(
            String operation, String params) throws Exception {

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope"
                + " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:tns=\"" + NAMESPACE + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<tns:" + operation + ">"
                + params
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
                os.write(xml.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            InputStream stream = status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (stream == null) {
                throw new IOException("HTTP status: " + status);
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
            try (InputStream is = stream) {
                response = factory.newDocumentBuilder().parse(is);
            }

            NodeList faults =
                    response.getElementsByTagNameNS("*", "Fault");

            if (faults.getLength() > 0) {
                throw new IOException(
                        "SOAP Fault: "
                        + faults.item(0).getTextContent());
            }

            if (status < 200 || status >= 300) {
                throw new IOException("HTTP status: " + status);
            }

            return response;

        } finally {
            conn.disconnect();
        }
    }

    // ==========================================
    // NHAN DANH SACH CHUOI
    // ==========================================
    static List<String> requestStringArray(
            String studentCode, String qCode)
            throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response =
                callSoap("requestStringArray", params);

        NodeList nodes =
                response.getElementsByTagNameNS("*", "return");

        if (nodes.getLength() == 0) {
            throw new IOException("Server khong tra ve du lieu!");
        }

        List<String> words = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            words.add(nodes.item(i).getTextContent().trim());
        }

        return words;
    }

    // ==========================================
    // GUI DANH SACH KET QUA
    // ==========================================
    static void submitCharacterStringArray(
            String studentCode,
            String qCode,
            List<String> data) throws Exception {

        StringBuilder params = new StringBuilder();

        params.append("<studentCode>")
                .append(escapeXml(studentCode))
                .append("</studentCode>");

        params.append("<qCode>")
                .append(escapeXml(qCode))
                .append("</qCode>");

        for (String value : data) {
            params.append("<data>")
                    .append(escapeXml(value))
                    .append("</data>");
        }

        Document response = callSoap(
                "submitCharacterStringArray",
                params.toString());

        NodeList nodes =
                response.getElementsByTagNameNS("*", "return");

        if (nodes.getLength() > 0) {
            System.out.println(
                    "Server response: "
                    + nodes.item(0).getTextContent());
        }
    }

    // ==========================================
    // XML ESCAPE
    // ==========================================
    static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
