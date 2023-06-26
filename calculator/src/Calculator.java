import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;
import static java.lang.System.out;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------


    /*
    * Vi adderar operander till stacken, stöter vi på en operator, ta två från stacken, genomför operation,
    * pusha tillbaka.
    * annars sparar vi token som ett värde i vår stack. Vi gör en parseDouble då token är en string,
    * men för att enkelt kunna utföra operation på ett token vill vi att det ska sparas i stack som ett värde.
    *
    */
    double evalPostfix(List<String> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : postfix) {
            if (isOperator(token)) {
                try {
                    double d1 = stack.pop();
                    double d2 = stack.pop();
                    double result = applyOperator(token, d1, d2);
                    stack.push(result);
                }catch (NoSuchElementException e) {
                    throw new IllegalArgumentException(Calculator.MISSING_OPERAND);
                }
            } else {
                double value = Double.parseDouble(token);
                stack.push(value);
            }
        }

        // Ändrade från "stack.size() != 1" till "stack.size() < 1"
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression");
        }

        return stack.pop();
    }

    // Enkel metod som "matchar" operation baserat på vilken typ av operator den får, token (operator) ges i input som String
    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    // Shunting Yard Algorithm.
    List<String> infix2Postfix(List<String> infix) {

        Deque<String> stack = new ArrayDeque<>();
        List<String> queue = new ArrayList<>();

        for(String token : infix){
            //Kolla först om token är en operator, om true, avgör precedence...
            if(isOperator(token)) {
                    /* I stacken sparar vi alla operators, först kollar vi om stacken är empty
                     * om stacken är empty, lägg till current operator, om inte, kolla så att
                     * först i stacken är en operator (kan vara parantes), om true, kolla
                     * så att current operator (token) har lägre precedence, om ja, lägg till i stack
                     * om ej (större eller equal), pop stackens översta och placera i kö, lägg in current
                     * token till stacken.
                     *
                     *
                     getPrecedence(stack.peek()) == getPrecedence(token)))
                     */
                if (!stack.isEmpty() && isOperator(stack.peek()) && hasGreaterPrecedence(token, stack.peek())){
                    queue.add(stack.pop());
                }
                stack.push(token);
            }

            // Om token är en vänsterParantes.
            else if (isLeftParenthesis(token)) {
                stack.push(token);
            }
            else if (isRightParenthesis(token)) {
                // Om current token är en högerparantes, kollar vi först så att stacken inte är tom, förutsatt
                // att den inte är det, då kollar vi så att toppen av stacken inte är en vänsterparantes, vi
                // gör detta ända tills stacken blir tom, eller tills vi påträffar en vänsterparantes.
                /**
                 * Uppdaterad metod, utbruten while loop som ny består av en metod.
                 */
                addStackPopToQueue(stack, queue);
                // Hittar vi en vänsterparantes tar vi bara bort den (vi gör inget med returvärdet från pop).
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
            // Annars är token en operand.
            else {
                queue.add(token);
            }
        }

        while (!stack.isEmpty()) {
            queue.add(stack.pop());
        }

        return queue;
    }

    public void addStackPopToQueue(Deque<String> stack, List<String> queue){
        while (!stack.isEmpty() && !isLeftParenthesis(stack.peek())) {
            queue.add(stack.pop());
        }
    }


    boolean isLeftParenthesis(String token) {
        return "(".equals(token);
    }

    boolean isRightParenthesis(String token) {
        return ")".equals(token);
    }

    boolean isOperator(String token) {
        return OPERATORS.contains(token);
    }

    // egen hjälpmetod för att ta två tokens och jämföra vilken som har störst precedence.
    // hade kunnat skriva i infixmetoden en "<=" operator istället...
    // Viktiga med denna är att fånga upp fallet där de är lika, och då använda metoden getAssociativity
    private boolean hasGreaterPrecedence(String op1, String op2) {
        int op1Precedence = getPrecedence(op1);
        int op2Precedence = getPrecedence(op2);
        if (op1Precedence == op2Precedence) {
            return isLeftAssociative(op1);
        } else if (isRightAssociative(op1)) {
            return op1Precedence < op2Precedence;
        } else {
            return op1Precedence <= op2Precedence;
        }
    }

    private static boolean isRightAssociative(String op) {
        return op.equals("^");
    }
    private boolean isLeftAssociative(String token) {
        return getAssociativity(token) == Assoc.LEFT;
    }


    // Ger "rank" (precedence) för en given operator, där högre precedence = högre prioritet (högre rang).
    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    // Ger associationen för en given token (input String).
    // för "^" är associationen från Höger.
    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".equals(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)
    List<String> tokenize(String expr) {

        StringBuilder sb = new StringBuilder();
        ArrayList<String> tokens = new ArrayList<>();
        char [] copy = expr.toCharArray();

        for(int i = 0; i < copy.length; i++){
            // Check for nulls (just precaution).
            if(notNull(copy[i])){
                // Kollar om nuvarande char är blanksteg. Om ja, ta det "översta/första" som ligger
                // i stringbuildern och lägg till det till tokens.
                if(isSpace(copy[i])){
                    if(sb.length() > 0){
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                    // Kollar om character är operator
                    // Använde här en annan "isOperator" metod som kollade chars. Finns längre ner i koden...
                } else if (isOperator(String.valueOf(copy[i]))){
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }

                    tokens.add(Character.toString(copy[i]));
                }
                // Om character är en siffra.
                if(Character.isDigit(copy[i])) {

                    if (sb.length() > 0 && !Character.isDigit(sb.charAt(sb.length() - 1))) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                    // om det är en siffra sist i sb, lägger vi till vår nya till sb.
                    sb.append(copy[i]);
                }
            }
        }
        // Lägger till sista token till listan om den ej är empty
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }

        return tokens;
    }

    // Hjälpmetod för att kolla om character är null
    public boolean notNull(Character ch){
        return ch != null;
    }

    // Hjälpmetod för att kolla om character är blanksteg.
    public boolean isSpace(Character ch) {
        return Character.isWhitespace(ch);
    }

    // Helper method to check if a character is an operator. Returns -1 if the character
    // is not found in the string "operators" and hence, not an operator.
    // Update 29 maj. lade till "^" till listan av operators.

//    public boolean isOperator(Character ch) {
//        String operators = "+-*/()^";
//        return operators.indexOf(ch) != -1;
//    }


}



