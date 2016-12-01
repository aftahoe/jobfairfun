package job.fair.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by annawang on 3/26/16.
 */
public class Page<T> implements Iterable<T> {
    private List content;
    private int totalElements;

    public Page(org.springframework.data.domain.Page springPage) {
        this.content = new ArrayList(springPage.getContent());
        this.totalElements = springPage.getNumberOfElements();
    }

    public Page(List content, int totalElements) {
        this.content = new ArrayList(content);
        this.totalElements = totalElements;
    }

    public Page() {
    }

    public List getContent() {
        return content;
    }

    public void setContent(List content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
