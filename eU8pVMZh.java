
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class eU8pVMZh {

    // ============================================================
    // 1. CONFIGURATION: Cau hinh Web Service
    // ============================================================
    static final String ENDPOINT =
            "http://36.50.135.242:2221/ObjectService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "eU8pVMZh";

    public static void main(String[] args) throws Exception {

        // ========================================================
        // 2. INPUT: Lay danh sach EmployeeY tu server
        // ========================================================
        List<EmployeeY> employees =
                requestListEmployeeY(STUDENT_CODE, QUESTION_CODE);

        System.out.println("Number of employees: " + employees.size());

        // ========================================================
        // 3. ALGORITHM: Sap xep tham nien giam dan
        // Ngay bat dau som hon dung truoc.
        // List.sort la stable sort: ngay bang nhau giu thu tu cu.
        // ========================================================
        employees.sort(
                (a, b) -> Long.compare(a.startDateMillis, b.startDateMillis)
        );

        for (EmployeeY employee : employees) {
            System.out.println(
                    employee.name + " | " + employee.startDate
            );
        }

        // ========================================================
        // 4. OUTPUT: Gui danh sach da sap xep ve server
        // ========================================================
        submitListEmployeeY(
                STUDENT_CODE, QUESTION_CODE, employees);

        System.out.println("Submission completed.");
    }

    // ============================================================
    // DATA MODEL: EmployeeY
    // Luu ten, ngay bat dau ban dau va gia tri de sap xep.
    // ============================================================
    static class EmployeeY {
        String name;
        String startDate;
        long startDateMillis;

        EmployeeY(String name, String startDate) throws Exception {
            this.name = name;
            this.startDate = startDate;
            this.startDateMillis = parseDate(startDate);
        }
    }

    // ============================================================
    // DATE HELPER: Chuyen XML date/dateTime thanh timestamp
    // Ho tro cac gia tri ngay theo dinh dang XML Schema.
    // ============================================================
    static long parseDate(String value) throws Exception {
        XMLGregorianCalendar calendar =
                DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(value);

        return calendar.toGregorianCalendar()
                .getTimeInMillis();
    }

    // ============================================================
    // SOAP HELPER: Gui SOAP request va nhan XML response
    // ============================================================
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
            // Gui request den server.
            try (OutputStream output = connection.getOutputStream()) {
                output.write(
                        requestXml.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();

            InputStream responseStream =
                    statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (responseStream == null) {
                throw new IOException(
                        "No response body. HTTP status: " + statusCode);
            }

            // Parse XML response va chan external entities.
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
    // XML HELPER: Lay text cua mot phan tu con truc tiep
    // So sanh local name de ho tro ca element co namespace.
    // ============================================================
    static String getChildText(Element parent, String wantedName) {
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

                if (wantedName.equals(localName)) {
                    return element.getTextContent().strip();
                }
            }
        }

        return null;
    }

    // ============================================================
    // INPUT: requestListEmployeeY(studentCode, qCode)
    // Doc cac object co hai truong name va startDate.
    // ============================================================
    static List<EmployeeY> requestListEmployeeY(
            String studentCode, String questionCode) throws Exception {

        String parametersXml =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(questionCode)
                + "</qCode>";

        Document response =
                callSoap("requestListEmployeeY", parametersXml);

        List<EmployeeY> employees = new ArrayList<>();
        NodeList elements = response.getElementsByTagName("*");

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);

            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;

            String name = getChildText(element, "name");
            String startDate = getChildText(element, "startDate");

            // Chi nhan phan tu dai dien cho EmployeeY.
            if (name != null && startDate != null) {
                employees.add(new EmployeeY(name, startDate));
            }
        }

        return employees;
    }

    // ============================================================
    // OUTPUT: submitListEmployeeY(studentCode, qCode, data)
    // Moi EmployeeY duoc gui voi name va startDate.
    // ============================================================
    static void submitListEmployeeY(
            String studentCode,
            String questionCode,
            List<EmployeeY> employees) throws Exception {

        StringBuilder parametersXml = new StringBuilder();

        parametersXml.append("<studentCode>")
                .append(escapeXml(studentCode))
                .append("</studentCode>");

        parametersXml.append("<qCode>")
                .append(escapeXml(questionCode))
                .append("</qCode>");

        for (EmployeeY employee : employees) {
            parametersXml.append("<data>")
                    .append("<name>")
                    .append(escapeXml(employee.name))
                    .append("</name>")
                    .append("<startDate>")
                    .append(escapeXml(employee.startDate))
                    .append("</startDate>")
                    .append("</data>");
        }

        callSoap(
                "submitListEmployeeY",
                parametersXml.toString());
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
