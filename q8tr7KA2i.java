
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class q8tr7KA2i {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/DataService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "8tr7KA2i";

    public static void main(String[] args) {
        try {
            // 1. Nhan danh sach so nguyen tu server
            List<Integer> numbers = getData(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input: " + numbers);

            // 2. Dem so nguyen to va tinh checksum
            int primeCount = 0;
            long checksum = 0;

            for (int i = 0; i < numbers.size(); i++) {
                int value = numbers.get(i);

                if (isPrime(value)) {
                    primeCount++;
                }

                checksum = (checksum
                        + (long) (i + 1) * value) % 100000;
            }

            // Chuan hoa checksum
            checksum = (checksum + 100000) % 100000;

            // 3. Tao chuoi ket qua
            String result = "primeCount=" + primeCount
                    + ";checksum=" + checksum;

            System.out.println("Output: " + result);

            // 4. Gui ket qua len server
            String status = submitDataString(
                    STUDENT_CODE, QUESTION_CODE, result);

            System.out.println("Status: " + status);
            System.out.println("Submission completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // ALGORITHM: Kiem tra so nguyen to
    // =============================================
    static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }

        if (n == 2) {
            return true;
        }

        if (n % 2 == 0) {
            return false;
        }

        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
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
    // INPUT: getData(studentCode, qCode)
    // =============================================
    static List<Integer> getData(
            String studentCode,
            String qCode) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response = callSoap("getData", params);

        List<Integer> numbers = new ArrayList<>();

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            throw new IOException(
                    "Server khong tra ve du lieu!");
        }

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);

            NodeList children = node.getChildNodes();
            boolean hasElementChild = false;

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if (child instanceof Element) {
                    hasElementChild = true;
                    String value = child.getTextContent().trim();

                    if (!value.isEmpty()) {
                        numbers.add(Integer.parseInt(value));
                    }
                }
            }

            if (!hasElementChild) {
                String value = node.getTextContent().trim();

                if (!value.isEmpty()) {
                    numbers.add(Integer.parseInt(value));
                }
            }
        }

        if (numbers.isEmpty()) {
            throw new IOException(
                    "Danh sach so nguyen rong!");
        }

        return numbers;
    }

    // =============================================
    // OUTPUT: submitDataString
    // =============================================
    static String submitDataString(
            String studentCode,
            String qCode,
            String data) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>"
                + "<data>" + escapeXml(data)
                + "</data>";

        Document response =
                callSoap("submitDataString", params);

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            return "No status returned";
        }

        return elements.item(0).getTextContent().trim();
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
