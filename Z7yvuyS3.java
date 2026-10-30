
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Z7yvuyS3 {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/DataService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "Z7yvuyS3";

    public static void main(String[] args) {
        try {
            // 1. Lay danh sach so nguyen
            List<Integer> numbers = getData(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input: " + numbers);

            // 2. Dem so chan va so le
            int even = 0;
            int odd = 0;

            for (int number : numbers) {
                if (number % 2 == 0) {
                    even++;
                } else {
                    odd++;
                }
            }

            // 3. Tao ket qua String[]
            String[] result = {
                "EVEN=" + even,
                "ODD=" + odd
            };

            System.out.println("Output: "
                    + Arrays.toString(result));

            // 4. Gui ket qua len server
            submitDataStringArray(
                    STUDENT_CODE, QUESTION_CODE, result);

            System.out.println("Submission completed.");

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

            int status = conn.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
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

            if (status < 200 || status >= 300) {
                throw new IOException("HTTP status: " + status);
            }

            return response;

        } finally {
            conn.disconnect();
        }
    }

    // ==================================================
    // INPUT: getData(studentCode, qCode)
    // ==================================================
    static List<Integer> getData(
            String studentCode, String qCode)
            throws Exception {

        String params =
                "<studentCode>" + studentCode + "</studentCode>"
                + "<qCode>" + qCode + "</qCode>";

        Document response = callSoap("getData", params);

        List<Integer> numbers = new ArrayList<>();

        // Doc cac phan tu return trong SOAP response
        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        for (int i = 0; i < elements.getLength(); i++) {
            String value = elements.item(i)
                    .getTextContent().trim();

            if (!value.isEmpty()) {
                numbers.add(Integer.parseInt(value));
            }
        }

        return numbers;
    }

    // ==================================================
    // OUTPUT: submitDataStringArray
    // ==================================================
    static void submitDataStringArray(
            String studentCode,
            String qCode,
            String[] data) throws Exception {

        StringBuilder params = new StringBuilder();

        params.append("<studentCode>")
                .append(studentCode)
                .append("</studentCode>");

        params.append("<qCode>")
                .append(qCode)
                .append("</qCode>");

        for (String value : data) {
            params.append("<data>")
                    .append(value)
                    .append("</data>");
        }

        callSoap("submitDataStringArray", params.toString());
    }
}
