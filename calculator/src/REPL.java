import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

/**
 *  A command line interface for the Calculator
 *
 *  REPL = Read Eval Print Loop
 *
 *   **** NOTHING TO DO HERE ****
 *
 */
class REPL {

    public static void main(String[] args) {
        new REPL().program();
    }

    final Scanner scan = new Scanner(in);
    final Calculator calculator = new Calculator();

    void program() {

        out.println("Calculator v 1.0. To exit input bye");
        while (true) {
            out.print("> ");
            String input = scan.nextLine();
            if( "bye".equals(input)){
                break;
            }
            try {
                double result = calculator.eval(input);
                out.println(result);
            }catch( Exception e){
                out.println(e.getMessage());
            }
        }
    }


}
