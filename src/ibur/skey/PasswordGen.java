package ibur.skey;

public class PasswordGen {
	private static final PwReq DEFAULT_REQ = new PwReq();
	
	public static String generatePassword(int pwlen) {
		return generatePassword(pwlen, DEFAULT_REQ);
	}
	
	public static String generatePassword(int pwlen, PwReq req) {
		char[] charset = req.getCharset();
		String res = "";
			char[] out = new char[pwlen];
			for(int i = 0; i < pwlen; i++) {
				out[i] = charset[Crypto.r.nextInt(charset.length)];
			}
			res = new String(out);
		return res;
	}

	public static class PwReq {
		private static final String COMMON_SYMBOLS = "~!@#$%^&*()-_+=";
		private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
		private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		private static final String NUMBERS = "0123456789";

		/**
		 * -1: none, 0: any number, 1: required
		 */
		public int lcase;
		public int ucase;
		public int numbers;
		public int symbols;

		String charset;
		
		public PwReq() {
			charset = "";
			charset += LOWERCASE_LETTERS;
			charset += UPPERCASE_LETTERS;
			charset += NUMBERS;
			charset += COMMON_SYMBOLS;
		}
		
		public PwReq(String scheme) {
			this.charset = "";
			String proc = "";
			boolean escaped = false;
			for(int i = 0; i < scheme.length(); i++) {
				char c = scheme.charAt(i);
				if(c == '-') {
					if(escaped) {
						proc += c;
						escaped = false;
					} else {
						proc += (char) 31;
					}
				} else if(c == '\\') {
					if(escaped) {
						proc += c;
						escaped = false;
					} else {
						escaped = true;
					}
				} else {
					escaped = false;
					proc += c;
				}
			}
			for(int i = 0; i < proc.length(); i++) {
				if(i < proc.length() - 2) {
					if(proc.charAt(i + 1) == 31) {
						char min = proc.charAt(i) < proc.charAt(i+2) ? proc.charAt(i) : proc.charAt(i+2);
						char max = proc.charAt(i) >= proc.charAt(i+2) ? proc.charAt(i) : proc.charAt(i+2);
						for(char c = min; c <= max; c++) {
							charset += c;
						}
						i += 2;
					} else {
						charset += proc.charAt(i);
					}
				} else {
					charset += proc.charAt(i);
				}
			}
			System.out.println(charset);
		}

		public char[] getCharset() {
			return charset.toCharArray();
		}
		
		public double getEntropy(int len) {
			return len * Math.log(getCharset().length)/Math.log(2);
		}
	}
}
