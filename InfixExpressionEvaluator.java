package cs445.a2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * This class uses two stacks to evaluate an infix arithmetic expression from an
 * InputStream.
 */
public class InfixExpressionEvaluator {
	
	//create a char, previousToken, to hold the previous operator
	char previousToken;
	
	//create a double, result, to hold the result of the expression
	double result = 0.0;
	
    // Tokenizer to break up our input into tokens
    StreamTokenizer tokenizer;

    // Stacks for operators (for converting to postfix) and operands (for
    // evaluating)
    StackInterface<Character> operators;
    StackInterface<Double> operands;

    /**
     * Initializes the solver to read an infix expression from input.
     */
    public InfixExpressionEvaluator(InputStream input) {
        // Initialize the tokenizer to read from the given InputStream
        tokenizer = new StreamTokenizer(new BufferedReader(
                        new InputStreamReader(input)));

        // Declare that - and / are regular characters (ignore their regex
        // meaning)
        tokenizer.ordinaryChar('-');
        tokenizer.ordinaryChar('/');

        // Allow the tokenizer to recognize end-of-line
        tokenizer.eolIsSignificant(true);

        // Initialize the stacks
        operators = new ArrayStack<Character>();
        operands = new ArrayStack<Double>();
    }

    /**
     * A type of runtime exception thrown when the given expression is found to
     * be invalid
     */
    class ExpressionError extends RuntimeException {
        ExpressionError(String msg) {
            super(msg);
        }
    }

    /**
     * Creates an InfixExpressionEvaluator object to read from System.in, then
     * evaluates its input and prints the result.
     */
    public static void main(String[] args) {
        InfixExpressionEvaluator solver =
                        new InfixExpressionEvaluator(System.in);
        Double value = solver.evaluate();
        if (value != null) {
            System.out.println(value);
        }
    }

    /**
     * Evaluates the expression parsed by the tokenizer and returns the
     * resulting value.
     */
    public Double evaluate() throws ExpressionError {
        // Get the first token. If an IO exception occurs, replace it with a
        // runtime exception, causing an immediate crash.
        try {
            tokenizer.nextToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Continue processing tokens until we find end-of-line
        while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
            // Consider possible token types
            switch (tokenizer.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    // If the token is a number, process it as a double-valued
                    // operand
                    processOperand((double)tokenizer.nval);
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                    // If the token is any of the above characters, process it
                    // is an operator
                    processOperator((char)tokenizer.ttype);
                    break;
                case '(':
                case '[':
                    // If the token is open bracket, process it as such. Forms
                    // of bracket are interchangeable but must nest properly.
                    processOpenBracket((char)tokenizer.ttype);
                    break;
                case ')':
                case ']':
                    // If the token is close bracket, process it as such. Forms
                    // of bracket are interchangeable but must nest properly.
                    processCloseBracket((char)tokenizer.ttype);
                    break;
                case StreamTokenizer.TT_WORD:
                    // If the token is a "word", throw an expression error
                    throw new ExpressionError("Unrecognized token: " +
                                    tokenizer.sval);
                default:
                    // If the token is any other type or value, throw an
                    // expression error
                    throw new ExpressionError("Unrecognized token: " +
                                    String.valueOf((char)tokenizer.ttype));
            }

            // Read the next token, again converting any potential IO exception
            try {
                tokenizer.nextToken();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Almost done now, but we may have to process remaining operators in
        // the operators stack
        processRemainingOperators();

        // Return the result of the evaluation
        return result;
    }
    
    /**
     * 
     * @param operator1 current token character
     * @param operator2 character on top of operator stack
     * @return true if operator2 has higher precedence than operator1, false if less than/equal precedence
     */
    private boolean greaterPrecedence(char operator1, char operator2) {
    	//return false if the top of the stack is an open parentheses (current token has higher precedence)
    	if (operator2 == '(' || operator2 == '[')
    		return false;
    	//return false if the top of the stack is a +- and the current token is */
    	else if ((operator1 == '*' || operator1 == '/') && (operator2 == '+' || operator2 == '-'))
    		return false;
    	//return false if the current token is an exponent operator
    	else if (operator1 == '^')
    			return false;
    	//otherwise return true
    	else
    		return true;
    }

    /**
     * Processes an operand.
     */
    void processOperand(double operand) {
    	//throw a new exception if the user enters two operands in a row
    	if (!operands.isEmpty() && operators.isEmpty())
    		throw new ExpressionError("Multiple operands entered in a row");
    	
    	if (previousToken == ')' || previousToken == ']')
    		throw new ExpressionError("Implied multiplication not supported");
    	
    	//push the operand
        operands.push(new Double(operand));
        
        //store the operand as the previous token
        previousToken = Double.toString(operand).charAt(0);
    }

    /**
     * Processes an operator.
     */
    void processOperator(char operator) {
    	//if the previous token wasn't an integer or closed parentheses, throw a new exception
    	if (!Character.isDigit(previousToken) && previousToken != ')' && previousToken != ']')
    		throw new ExpressionError("Multiple operators entered in a row");
    	
    	//while the operator stack isn't empty and the current token has lower precedence than the operator stack's top token
    	while (!operators.isEmpty() && greaterPrecedence(operator, operators.peek())) {
    		//pop the top operator and top 2 operands
    		char top = operators.pop();
        	double operand1 = operands.pop();
        	double operand2 = operands.pop();
        
        	//perform the correct arithmetic and repush the operand
       		switch(top) {
        		case '+': operands.push(operand2+operand1);
        		break;
        		case '-': operands.push(operand2-operand1);
        		break;
        		case '*': operands.push(operand2*operand1);
        		break;
        		case '/': operands.push(operand2/operand1);
        		break;
        		case '^': operands.push(Math.pow(operand2, operand1));
        		break;
        	}
        }
    	
    	//push the operator onto the stack when the operator stack is empty or the current token has highest precedence
    	operators.push(operator);
    	
    	//store the operator as the previous token
    	previousToken = operator;
    }

    /**
     * Processes an open bracket.
     */
    void processOpenBracket(char openBracket) {
    	//if the previous token was an integer, inform the user that implied multiplication does not work in this program
    	if (Character.isDigit(previousToken))
    			throw new ExpressionError("Implied multiplication not supported");
    	
    	//push the open bracket onto the operator stack
    	operators.push(openBracket);
    	
    	//store the operator as the previous token
    	previousToken = openBracket;
    }

    /**
     * Processes a close bracket.
     */
    void processCloseBracket(char closeBracket) {
    	//throw an Expression Error if there is no operator in the operator stack
    	if (operators.isEmpty())
    		throw new ExpressionError("Open bracket missing");
    	
    	//while the top of the operator stack isn't an open parentheses or bracket
    	while (operators.peek() != '(' && operators.peek() != '[') {
    		//pop the operator stack and operand stack twice
        	char top = operators.pop();
        	double operand1 = operands.pop();
        	double operand2 = operands.pop();
        
        	//perform the relevant arithmetic and repush the operand
       		switch(top) {
        		case '+': operands.push(operand2+operand1);
        		break;
        		case '-': operands.push(operand2-operand1);
        		break;
        		case '*': operands.push(operand2*operand1);
        		break;
        		case '/': operands.push(operand2/operand1);
        		break;
        		case '^': operands.push(Math.pow(operand2, operand1));
        		break;
        	}
       		
        	//throw an Expression Error if there is no operator in the operator stack
        	if (operators.isEmpty())
        		throw new ExpressionError("Open bracket missing");
    	}
    	
    	//pop the open parentheses when it is found
    	char popped = operators.pop();
    	
    	//ensure the closed bracket was of the correct type, and if not, throw an ExpressionError
    	if (popped == '(') {
    		if (closeBracket == ']')
    			throw new ExpressionError("Incorrect bracket format");
    	}
    	else {
    		if (closeBracket == ')')
    			throw new ExpressionError("Incorrect bracket format");
    	}
    	
    	//store the closed bracket as the previous operator
    	previousToken = closeBracket;
    }

    /**
     * Processes any remaining operators leftover on the operators stack
     */
    void processRemainingOperators() {
    	//while the operator stack still has tokens
        while (!operators.isEmpty()) {
        	//throw an error if an expression is missing a closing bracket
        	if (operators.peek() == '(' || operators.peek() == '[')
        		throw new ExpressionError("Expression missing closed bracket");
        	
        	//pop the operator stack and the operand stack twice
        	char top = operators.pop();
        	double operand1 = operands.pop();
        	double operand2 = operands.pop();
        
        	//perform relevent arithmetic and repush the operand
       		switch(top) {
        		case '+': operands.push(operand2+operand1);
        		break;
        		case '-': operands.push(operand2-operand1);
        		break;
        		case '*': operands.push(operand2*operand1);
        		break;
        		case '/': operands.push(operand2/operand1);
        		break;
        		case '^': operands.push(Math.pow(operand2, operand1));
        		break;
       		}
        }
        
        //pop the final operand and store it in result
        result = operands.pop();
    }
}