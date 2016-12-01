package job.fair.jobfair.testutil.connector;

import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Job;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by annawang on 1/19/16.
 */
public class HtmlConnector51job {


    public String readURLhtml(String url) throws IOException {

        Document doc = Jsoup.parse(new URL(url).openStream(), "gb2312", url);
        doc.select("br").append("\\n");
        doc.select("p").prepend("\\n\\n");
        Elements jobscriptions = doc.select("body").select("div.tCompanyPage").select("div.tCompany_center.clearfix")
                .select("div.tCompany_main").select("div.tBorderTop_box");
        StringBuilder descpBuilder = new StringBuilder();
        for (Element e : jobscriptions) {
            if (e.attr("class").equals("tBorderTop_box")) {
                Elements i = e.select("div.bmsg.job_msg.inbox");
                //TODO why some of it doesn't work now
                if (i.size() > 0) {
                    Element eee = i.get(0);
                    for (Node child : eee.childNodes()) {
                        if (child instanceof TextNode) {
                            String text = ((TextNode) child).text();
                            descpBuilder.append(text).append("\n");
                        }
                    }
                }
                break;
            }
        }
        return descpBuilder.toString();
    }


    public Set<Company> parseHtmlFile(String filename) throws IOException {
        File input = new File(filename);
        Document doc = Jsoup.parse(input, "gb2312");
        Elements listingTable = doc.select("body").select("div").select("div.dw_table").select("div.el");
        Map<String, Company> companies = new HashMap<>();
        int count = 0;
        for (Element e : listingTable) {
            if (e.attr("class").equals("el")) {
                Job job = createJob(e);
                String companyLink = e.child(1).child(0).attr("href");
                String companyName = e.child(1).child(0).attr("title");
                if (companies.containsKey(companyName)) {
                    Company company = companies.get(companyName);
                    company.addJob(job);
                } else {
                    // create a new company
                    count++;
                    Company company = new Company();
                    company.setName(companyName);
                    //TODO
                    //TestUtil.setEvaludationQuestions(company);
                    company.setLink(companyLink);
                    company.addJob(job);
                    company.setIcon("http://172.16.8.146:8080/jobfair/company/icon/" + count + ".png");
                    companies.put(companyName, company);
                }
            }
        }
        return new HashSet<>(companies.values());
    }

    //TODO does builder work?
    private Job createJob(Element e) throws IOException {
        Random randomGenerator = new Random();
        Job job = new Job();
        job.setPositionCount(randomGenerator.nextInt(7));
        job.setPositionNumber(Integer.parseInt(e.child(0).child(0).attr("value")));
        job.setPositionName(e.child(0).child(1).attr("title"));
        job.setLocation(e.child(2).html());
        //TODO
        //job.setCompensation(e.child(3).html());
        job.setIssueDate(e.child(4).html());

        //position link e.child(0).child(1).attr("href")
        String desp = readURLhtml(e.child(0).child(1).attr("href"));
        job.setDescription(desp);

        // Add skills for job
        job.addRequiredSkills("HTML");
        job.addRequiredSkills("JAVA");
        job.addRequiredSkills("Spring");

        return job;
    }


}
