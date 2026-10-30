
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class mVELbWLJ {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/CharacterService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "mVELbWLJ";

    public static void main(String[] args) {
        try {
            // 1. Nhan danh sach log
            List<String> logs = requestStringArray(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input:");
            for (String log : logs) {
                System.out.println(log);
            }

            // 2. Che thong tin nhay cam
            List<String> result = new ArrayList<>();

            for (String log : logs) {
                String masked = maskLog(log);
                result.add(masked);
            }

            // 3. Sap xep ERROR -> WARN -> INFO
            result.sort(Comparator.comparingInt(
                    mVELbWLJ::getPriority));

            System.out.println("\nOutput:");
            for (String log : result) {
                System.out.println(log);
            }

            // 4. Gui ket qua ve server
            String status = submitStringArray(
                    STUDENT_CODE, QUESTION_CODE, result);

            System.out.println("\nStatus: " + status);
            System.out.println("Submission completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // ALGORITHM: Che thong tin nhay cam
    // =============================================
    static String maskLog(String log) {

        // Che email
        log = log.replaceAll(
                "(?i)(\\bemail\\s*=\\s*)\\S+",
                "$1[EMAIL]");

        // Che so dien thoai
        log = log.replaceAll(
                "(?i)(\\bphone\\s*=\\s*)\\S+",
                "$1[PHONE]");

        // Che token
        log = log.replaceAll(
                "(?i)(\\btoken\\s*=\\s*)\\S+",
                "$1[TOKEN]");

        return log;
    }

    // =============================================
    // ALGORITHM: Do uu tien cua log
    // =============================================
    static int getPriority(String log) {
        String level = log.trim().toUpperCase(Locale.ROOT);

        if (level.startsWith("ERROR")) {
            return 0;
        }
        if (level.startsWith("WARN")) {
            return 1;
        }
        if (level.startsWith("INFO")) {
            return 2;
        }

        return 3;
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
                throw new IOException(
                        "SOAP Fault: "
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

    // =============================================
    // INPUT: requestStringArray
    // =============================================
    static List<String> requestStringArray(
            String studentCode,
            String qCode) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response =
                callSoap("requestStringArray", params);

        List<String> logs = new ArrayList<>();

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            NodeList children = node.getChildNodes();

            boolean hasElementChild = false;

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if (child instanceof Element) {
                    hasElementChild = true;

                    String value =
                            child.getTextContent().trim();

                    if (!value.isEmpty()) {
                        logs.add(value);
                    }
                }
            }

            if (!hasElementChild) {
                String value =
                        node.getTextContent().trim();

                if (!value.isEmpty()) {
                    logs.add(value);
                }
            }
        }

        if (logs.isEmpty()) {
            throw new IOException(
                    "Server khong tra ve danh sach log!");
        }

        return logs;
    }

    // =============================================
    // OUTPUT: submitStringArray
    // =============================================
    static String submitStringArray(
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
                "submitStringArray",
                params.toString());

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
