/**
 * 
 */
package c0;

/**
 * @author ftrww
 *
 */
public class SyntacticorException extends Exception {
	private String msg;

	public SyntacticorException(String msg) {
		this.msg = msg;
	}

	public String toString()
	{
		return "Tokenizer error "+ this.msg;
	}
	
}	
