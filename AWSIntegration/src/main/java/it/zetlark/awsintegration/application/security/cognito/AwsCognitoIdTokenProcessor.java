package it.zetlark.awsintegration.application.security.cognito;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import it.zetlark.awsintegration.application.security.JwtAuthentication;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.List.of;

@Component
public class AwsCognitoIdTokenProcessor {

	@Autowired
	private AwsCognitoJwtConfiguration awsCognitoJwtConfiguration;

	@Autowired
	private ConfigurableJWTProcessor configurableJWTProcessor;

	public Authentication authenticate(HttpServletRequest request) throws Exception {
		String idToken = request.getHeader(this.awsCognitoJwtConfiguration.getHttpHeader());
		if (idToken != null) {
			JWTClaimsSet claims = this.configurableJWTProcessor.process(this.getBearerToken(idToken), null);
			validateIssuer(claims);
			verifyIfIdToken(claims);
			String username = getUserNameFrom(claims);
			if (username != null) {
				List<GrantedAuthority> grantedAuthorities = getGrantAuthoritiesFrom(claims);
				User user = new User(username, "", of());
				return new JwtAuthentication(user, claims, grantedAuthorities);
			}
		}
		return null;
	}

	private List<GrantedAuthority> getGrantAuthoritiesFrom(JWTClaimsSet claims) {

		return ((JSONArray) claims.getClaims().get("cognito:groups")).stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" +role.toString())).distinct().collect(Collectors.toList());
	}

	private String getUserNameFrom(JWTClaimsSet claims) {
		return claims.getClaims().get(this.awsCognitoJwtConfiguration.getUserNameField()).toString();
	}


	private void verifyIfIdToken(JWTClaimsSet claims) throws Exception {
		if (!claims.getIssuer().equals(this.awsCognitoJwtConfiguration.getCognitoIdentityPoolUrl())) {
			throw new Exception("JWT Token is not an ID Token");
		}
	}

	private void validateIssuer(JWTClaimsSet claims) throws Exception {
		if (!claims.getIssuer().equals(this.awsCognitoJwtConfiguration.getCognitoIdentityPoolUrl())) {
			throw new Exception(String.format("Issuer %s does not match cognito idp %s", claims.getIssuer(), this.awsCognitoJwtConfiguration.getCognitoIdentityPoolUrl()));
		}
	}

	private String getBearerToken(String token) {
		return token.startsWith("Bearer ") ? token.substring("Bearer ".length()) : token;
	}
}
