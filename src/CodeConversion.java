/**
 * 
 */


/**
 * @author ftrww
 *
 */
public class CodeConversion{
	private CodeConversion() {};
	
	public static final String nop = "00";
	public static final String push(long num) {
		return "01" + String.format("%016x", num);
	}

	public static final String popn = "03";
	public static final String dup = "04";
	public static final String loca(int num) {
		return "0a" + String.format("%08x",num);
	}
	public static final String arga(int num) {
		return "0b" + String.format("%08x",num);
	}
	public static final String globa(int num) {
		return "0c" + String.format("%08x", num);
	}
	public static final String load8 = "10";
	public static final String load16 = "11";
	public static final String load32 = "12";
	public static final String load64 = "13";
	public static final String store8 = "14";
	public static final String store16 = "15";
	public static final String store32 = "16";
	public static final String store64 ="17";
	public static final String alloc = "18";
	public static final String free = "19";
	public static final String stackalloc(int num) {
		return "1a" + String.format("%08x", num);
	}
	public static final String addi = "20";
	public static final String subi = "21";
	public static final String muli = "22";
	public static final String divi = "23";
	public static final String addf = "24";
	public static final String subf = "25";
	public static final String mulf = "26";
	public static final String divf = "27";
	public static final String divu = "28";
	public static final String shl = "29";
	public static final String shr = "2a";
	public static final String and = "2b";
	public static final String or = "2c";
	public static final String xor = "2d";
	public static final String not = "2e";
	public static final String cmpi = "30";
	public static final String cmpu = "31";
	public static final String cmpf = "32";
	public static final String negi = "34";
	public static final String negf = "35";
	public static final String itof = "36";
	public static final String ftoi = "37";
	public static final String shrl = "38";
	public static final String setlt = "39";
	public static final String setgt = "3a";
	
	public static final String br(int num) {
		if(num >= 0) {
			return "41" + String.format("%08x", num);
		}
		else {
			long a  = Integer.MAX_VALUE + num + 1;
			a = a +((long)1<<31);
			return "41" + String.format("%08x", a);
		}
	}
	public static final String br(String s) {
		return "41" + s;
	}
	public static final String brfalse(int num) {
		return "42" + String.format("%08x", num);
	}
	public static final String brtrue(int num) {
		return "43" + String.format("%08x",num);
	}
	public static final String call(int num) {
		return "48" + String.format("%08x", num);
	}
	public static final String ret = "49";
	public static final String callname(int num) {
		return "4a" + String.format("%08x", num);
	}
	public static final String scani = "50";
	public static final String scanc = "51";
	public static final String scanf = "52";
	public static final String printi = "54";
	public static final String printc = "55";
	public static final String printf = "56";
	public static final String prints = "57";
	public static final String println = "58";
	public static final String panic = "fe";
	public static final String breakFlag = "<break>";
	public static final String contninueFlag = "<continue>";
	public static final String whileStartFlag = "<while_start>";
	public static final String whileEndFlag = "<while_end>";
}


//final class CodeConversion {
//    private CodeConversion() {}
//
//    final public static String nop = "00";
//    final public static String push(long num){
//        return "push(" + num + ")";
//    }
//    final public static String popn = "popn";
//    final public static String dup = "dup";
//    final public static String loca(int num){
//        return "loca(" + num + ")";
//    }
//    final public static String arga(int num){
//        return "arga(" + num + ")";
//    }
//    final public static String globa(int num){
//        return "globa(" + num + ")";
//    }
//    final public static String load8 = "load8";
//    final public static String load16 = "11";
//    final public static String load32 = "12";
//    final public static String load64 = "load64";
//    final public static String store8 = "14";
//    final public static String store16 = "15";
//    final public static String store32 = "16";
//    final public static String store64 = "store64";
//    final public static String alloc = "18";
//    final public static String free = "19";
//    final public static String stackalloc(int num){
//        return "stackalloc(" + num + ")";
//    }
//    final public static String addi = "addi";
//    final public static String subi = "subi";
//    final public static String muli = "muli";
//    final public static String divi = "divi";
//    final public static String addf = "addf";
//    final public static String subf = "subf";
//    final public static String mulf = "mulf";
//    final public static String divf = "divf";
//    final public static String divu = "28";
//    final public static String shl = "29";
//    final public static String shr = "2a";
//    final public static String and = "2b";
//    final public static String or = "2c";
//    final public static String xor = "2d";
//    final public static String not = "not";
//    final public static String cmpi = "cmpi";
//    final public static String cmpu = "31";
//    final public static String cmpf = "cmpf";
//    final public static String negi = "negi";
//    final public static String negf = "negf";
//    final public static String itof = "itof";
//    final public static String ftoi = "ftoi";
//    final public static String shrl = "38";
//    final public static String setlt = "setlt";
//    final public static String setgt = "setgt";
//    final public static String br(int num){
//        return "br(" + num + ")";
//    }
//    final private static String br(String string){
//        return "41" + string;
//    }
//    final public static String brfalse(int num){
//        return "42" + String.format("%08x", num);
//    }
//    final public static String brtrue(int num){
//        return "brtrue(" + num + ")";
//    }
//    final public static String call(int num){
//        return "call(" + num + ")";
//    }
//    final public static String ret = "ret";
//    final public static String callname(int num){
//        return "4a" + String.format("%08x", num);
//    }
//    final public static String scani = "scani";
//    final public static String scanc = "scanc";
//    final public static String scanf = "scanf";
//    final public static String printi = "printi";
//    final public static String printc = "printc";
//    final public static String printf = "printf";
//    final public static String prints = "prints";
//    final public static String println = "println";
//    final public static String panic = "fe";
//    final public static String breakFlag = "<break>";
//    final public static String contninueFlag = "<continue>";
//    final public static String whileStartFlag = "<while start>";
//    final public static String whileEndFlag = "<while end>";
//}
