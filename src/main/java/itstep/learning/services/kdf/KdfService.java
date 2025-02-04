package itstep.learning.services.kdf;
// https://datatracker.ietf.org/doc/html/rfc2898
public interface KdfService {
    String dk(String password, String salt);
}
