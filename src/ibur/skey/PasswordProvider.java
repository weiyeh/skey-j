package ibur.skey;

public interface PasswordProvider {
	public byte[] getPassword(String prompt);
}
