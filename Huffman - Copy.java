
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implements construction, encoding, and decoding logic of the Huffman coding
 * algorithm. 
 */
public class Huffman {

    BinaryMinHeapImpl<Integer, Node> heap;
    Node root;
    Map<Character, Integer> freqs;
    HashMap<Character, String> code = new HashMap<>();
    boolean compressed = false;
    double totalInput;
    double totalOutput;
    int languageLength;

    /**
     * Constructs a {@code Huffman} instance from a seed string, from which to
     * deduce the alphabet and corresponding frequencies.
     * <p/>
     *
     * @param seed the String from which to build the encoding
     * @throws IllegalArgumentException seed is null, seed is empty, or resulting
     *                                  alphabet only has 1 character
     */
    public Huffman(String seed) {
        if (seed == null || seed.length() == 0) {
            throw new IllegalArgumentException();
        } else {
            this.freqs = new HashMap<>();
            for (int i = 0; i < seed.length(); i++) {
                languageLength++;
                if (freqs.containsKey(seed.charAt(i))) {
                    freqs.put(seed.charAt(i), freqs.get(seed.charAt(i)) + 1);
                } else {
                    freqs.put(seed.charAt(i), 1);
                }
            }
            if (freqs.size() < 2) {
                throw new IllegalArgumentException();
            }

            this.heap = new BinaryMinHeapImpl<>();

            Iterator iter = this.freqs.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry entry = (Entry) iter.next();
                Node x = new Node((int) entry.getValue(), (char) entry.getKey(), null, null);
                heap.add((int) entry.getValue(), x);
            }

            while (heap.size() > 1) {
                BinaryMinHeap.Entry<Integer, Node> first = this.heap.extractMin();
                BinaryMinHeap.Entry<Integer, Node> second = this.heap.extractMin();
                Node x = new Node((int) first.key + second.key, null, first.value, second.value);
                heap.add((int) first.key + second.key, x);
            }
            this.root = heap.extractMin().value;
            Map<Character, String> code = new HashMap<Character, String>();

            makeCode(this.root, new StringBuilder());

        }
    }

    /**
     * Constructs a {@code Huffman} instance from a frequency map of the input
     * alphabet.
     * <p/>
  
     *
     * @param alphabet a frequency map for characters in the alphabet
     * @throws IllegalArgumentException if the alphabet is null, empty, has fewer
     *                                  than 2 characters, or has any non-positive
     *                                  frequencies
     */

    public Huffman(Map<Character, Integer> alphabet) {
        if (alphabet == null || alphabet.size() < 2) {
            throw new IllegalArgumentException();
        }

        this.heap = new BinaryMinHeapImpl<>();
        this.freqs = alphabet;
        Iterator iter = this.freqs.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();

            if ((int) entry.getValue() <= 0) {
                throw new IllegalArgumentException();
            }
            languageLength += (int) entry.getValue();
            Node x = new Node((int) entry.getValue(), (char) entry.getKey(), null, null);
            heap.add((int) entry.getValue(), x);
        }

        while (heap.size() > 1) {
            BinaryMinHeap.Entry<Integer, Node> first = this.heap.extractMin();
            BinaryMinHeap.Entry<Integer, Node> second = this.heap.extractMin();
            Node x = new Node((int) first.key + second.key, null, first.value, second.value);
            heap.add((int) first.key + second.key, x);
        }
        this.root = heap.extractMin().value;

        makeCode(this.root, new StringBuilder());
    }

    public void makeCode(Node currNode, StringBuilder curr) {

        if (currNode.left != null) {
            makeCode(currNode.left, new StringBuilder(curr).append('0'));
        }
        if (currNode.right != null) {
            makeCode(currNode.right, new StringBuilder(curr).append('1'));
        }
        if (currNode.val != null) {
            this.code.put(currNode.val, curr.toString());
        }

    }

    /**
     * Compresses the input string.
     *
     * @param input the string to compress, can be the empty string
     * @return a string of ones and zeroes, representing the binary encoding of the
     *         inputted String.
     * @throws IllegalArgumentException if the input is null or if the input
     *                                  contains characters that are not
     *                                  compressible
     */
    public String compress(String input) {
        this.compressed = true;
        if (input == null) {
            throw new IllegalArgumentException();
        } else if (input.length() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            if (this.code.containsKey(input.charAt(i))) {
                result.append(this.code.get(input.charAt(i)));
                this.totalInput += 16;
                this.totalOutput += this.code.get(input.charAt(i)).length();
            } else {
                throw new IllegalArgumentException();
            }
        }
        return result.toString();

    }

    /**
     * Decompresses the input string.
     *
     * @param input the String of binary digits to decompress, given that it was
     *              generated by a matching instance of the same compression
     *              strategy
     * @return the decoded version of the compressed input string
     * @throws IllegalArgumentException if the input is null, or if the input
     *                                  contains characters that are NOT 0 or 1, or
     *                                  input contains a sequence of bits that is
     *                                  not decodable
     */
    public String decompress(String input) {
        if (input == null) {
            throw new IllegalArgumentException();
        } else if (input.length() == 0) {
            return "";
        }
        int i = 0;
        Node temp = this.root;
        StringBuilder result = new StringBuilder();

        while (i < input.length()) {
            if (input.charAt(i) == '1') {
                if (temp.right != null) {
                    temp = temp.right;
                    i++;
                } else {
                    if (temp.val == null) {
                        throw new IllegalArgumentException();
                    }
                    result.append(temp.val);
                    temp = this.root;
                }
            } else if (input.charAt(i) == '0') {
                if (temp.left != null) {
                    temp = temp.left;
                    i++;
                } else {
                    if (temp.val == null) {
                        throw new IllegalArgumentException();
                    }
                    result.append(temp.val);
                    temp = this.root;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (temp.val == null) {
            throw new IllegalArgumentException();
        }
        result.append(temp.val);

        return result.toString();

    }

    /**
     * Computes the compression ratio so far. 
     *
     * @return the ratio of the total output length to the total input length in
     *         bits
     * @throws IllegalStateException if no calls have been made to
     *                               {@link #compress(String)} before calling this
     *                               method
     */
    public double compressionRatio() {
        if (!compressed) {
            throw new IllegalStateException();
        }
        return this.totalOutput / this.totalInput;

    }

    /**
     * Computes the expected encoding length of an arbitrary character in the
     * alphabet based on the objective function of the compression.
     * <p>
     *
     * @return the expected encoding length of an arbitrary character in the
     *         alphabet
     */
    public double expectedEncodingLength() {

        Iterator iter = this.freqs.entrySet().iterator();
        double length = 0;

        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();
            double x = ((double) (int) entry.getValue() / (languageLength));
            length += x * code.get(entry.getKey()).length();
        }
        return length;

    }

    public class Node {
        int freq;
        Character val;
        Node left;
        Node right;

        public Node(int f, Character v, Node l, Node r) {
            this.freq = f;
            this.val = v;
            this.left = l;
            this.right = r;
        }

    }
}
