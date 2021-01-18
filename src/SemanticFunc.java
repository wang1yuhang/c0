/**
 * 
 */
/**
 * @author ftrww
 *
 */
public class SemanticFunc {
	public static byte checkTy(String ident) throws Exception{
		switch(ident) {
		case "int":
			return TableItem.INT;
		case "double":
			return TableItem.DOUBLE;
		case "void":
			return TableItem.VOID;
		default:
			throw new SemanticException("the type is not right");
		}
	}
}
