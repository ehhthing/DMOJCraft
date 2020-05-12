package science.larry.dmojcraft.dmoj;


import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import science.larry.dmojcraft.exceptions.InvalidSessionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserSession {
    private final static String BASE_URL = "https://dmoj.ca";
    private final static List<String> gradingStatuses = Arrays.asList("QU", "P", "G");

    private String token;
    private String user;

    public UserSession(String token) throws InvalidSessionException, IOException {
        this.token = token;
        Document doc;
        try {
            doc = Jsoup.connect(BASE_URL + "/user")
                    .header("Authorization", "Bearer " + token)
                    .followRedirects(true)
                    .get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401 || e.getStatusCode() == 400) {
                throw new InvalidSessionException();
            } else {
                throw e;
            }
        }
        this.user = doc.getElementById("user-links").getElementsByTag("b").first().html();
    }

    public Connection getAuthRequest(String url) {
        return Jsoup.connect(url)
                .header("Authorization", "Bearer " + token);
    }

    public int submit(String problem, int language, String code) throws IOException, InvalidSessionException {
        String submitURL = BASE_URL + "/problem/" + problem + "/submit";

        Document doc;
        try {
            doc = getAuthRequest(submitURL).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401) {
                throw new InvalidSessionException();
            } else {
                throw e;
            }
        }

        String problemID = doc.getElementById("id_problem").val();

        Response submitRes;
        try {
            submitRes = getAuthRequest(submitURL)
                    .method(Connection.Method.POST)
                    .data("source", code)
                    .data("language", String.valueOf(language))
                    .followRedirects(true)
                    .execute();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401) {
                throw new InvalidSessionException();
            } else {
                throw e;
            }
        }

        String[] path = submitRes.url().getPath().split("/");
        return Integer.parseInt(path[path.length - 1]);
    }

    public SubmissionResult getTestcaseStatus(int id) throws IOException, InvalidSessionException {
        Document submissionResult;
        try {
            submissionResult = getAuthRequest(BASE_URL + "/widgets/single_submission?id=" + id).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401) {
                throw new InvalidSessionException();
            } else {
                throw e;
            }
        }

        SubmissionResult res = new SubmissionResult();
        res.status = submissionResult.getElementsByClass("status").first().text();
        res.time = submissionResult.getElementsByClass("time").last().text();
        res.memory = submissionResult.getElementsByClass("memory").first().text();
        res.done = !gradingStatuses.contains(res.status);

        if (res.memory.equals("---")) {
            res.memory = null;
        }

        if (res.time.equals("---")) {
            res.time = null;
        }

        res.problemName = submissionResult.getElementsByClass("name").first().text();

        Document caseStatus;

        try {
            caseStatus = getAuthRequest(BASE_URL + "/widgets/submission_testcases?id=" + id).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401) {
                throw new InvalidSessionException();
            } else {
                throw e;
            }
        }

        res.raw_result = caseStatus.body().text();

        Element caseTable = caseStatus.getElementsByTag("tbody").first();

        if (caseTable == null) {
            res.cases = new ArrayList<>();
            return res;
        }

        List<Testcase> cases = new ArrayList<>();
        for (Element row : caseTable.children()) {
            Testcase testcase = new Testcase();
            try {
                testcase.id = Integer.parseInt(row.id());
            } catch (NumberFormatException e) {
                continue; // output row, so skip
            }
            testcase.descriptor = row.child(0).text();
            testcase.status = row.child(1).text();
            testcase.details = row
                    .children()
                    .subList(2, row.childrenSize())
                    .stream()
                    .map(Element::text)
                    .collect(Collectors.joining(" "));
            cases.add(testcase);
        }

        res.cases = cases;
        return res;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
