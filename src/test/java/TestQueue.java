import java.util.LinkedList;
import java.util.Queue;

public class TestQueue {

    static Queue<String> queue = new LinkedList<>();

    public static void main(String[] args) {
        queue.add("1");
        queue.add("2");
        queue.add("3");
        System.out.println(queue);
        System.out.println(queue.poll());
        System.out.println(queue);
    }
}
