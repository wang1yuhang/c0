/**
 * 
 */


/**
 * @author ftrww
 *
 */
public class SemanticException extends Exception{
	private String msg;

	public SemanticException(String msg) {
		this.msg = msg;
	}

	public String toString()
	{
		return "Semantic error "+ this.msg;
	}
}
