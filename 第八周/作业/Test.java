package zuoye8;

/**
 * 遍历node1，看node1的子链是否是node2的尾链
 * 时间复杂度O(m*n)
 */
public class Test {
    static class Node<T> {
        private T t;
        private Node next;

        public Node() {
        }

        public Node(T t) {
            this.t = t;
        }

        public Node<T> next() {
            return next;
        }

        public Node<T> next(Node<T> next) {
            this.next = next;
            return next;
        }

        public int count() {
            int count = 1;
            Node curNode = this;
            while (curNode.next != null) {
                count++;
                curNode = curNode.next;
            }
            return count;
        }

    }


    public static boolean isTail(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        Node n1 = node1;
        Node n2 = node2;
        while (n1 != n2 && n1 != null) {
            n1 = n1.next();
        }
        while (n1 != null && n2 != null) {
            if (n1 == n2) {
                n1 = n1.next();
                n2 = n2.next();
            } else {
                return false;
            }
        }
        if (n1 != null || n2 != null) {
            return false;
        } else {
            return true;
        }
    }

    public static Node getCommonNode(Node node1, Node node2) {
        int count1 = node1.count();
        int count2 = node2.count();
        Node n1 = null;
        Node n2 = null;
        if (count1 > count2) {
            n1 = node1;
            n2 = node2;
        } else {
            n1 = node2;
            n2 = node1;
        }
        while (!isTail(n1, n2)) {
            n2 = n2.next();
        }
        return n2;

    }

    public static void main(String[] args) {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        Node d = new Node("d");
        Node e = new Node("e");
        Node f = new Node("f");
        Node x = new Node("x");
        Node y = new Node("y");
        Node z = new Node("z");
        a.next(b).next(c).next(x).next(y).next(z);
        d.next(e).next(f).next(x).next(y).next(z);
        Node commonNode = getCommonNode(a, d);
        if (commonNode != null) {
            System.out.println("has common node");
        } else {
            System.out.println("no common node");
        }
    }
}
