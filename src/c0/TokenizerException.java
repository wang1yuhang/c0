/**
 * 
 */
package c0;

/**
 * @author ftrww
 *
 */
public class TokenizerException extends Exception {
	private String msg;

	public TokenizerException(String msg) {
		this.msg = msg;
	}

	public String toString()
	{
		return "Tokenizer error "+ this.msg;
	}
}
