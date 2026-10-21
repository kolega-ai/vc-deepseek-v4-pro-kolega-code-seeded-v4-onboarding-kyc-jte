package com.meridianbank.onboarding.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only access to the corporate staff directory (LDAP) used to prefill staff onboarding: given
 * the employee reference entered on the admin form, the matching directory entry's uid, cn and mail
 * are returned so the branch and role can be confirmed before the account is created.
 */
@Service
public class StaffDirectoryService {

    private final String directoryUrl;
    private final String baseDn;
    private final String bindPrincipal;
    private final String bindCredential;

    public StaffDirectoryService(
            @Value("${app.directory.url:ldap://directory.meridianbank.internal:389}") String directoryUrl,
            @Value("${app.directory.base-dn:ou=staff,dc=meridianbank,dc=internal}") String baseDn,
            @Value("${app.directory.bind-principal:cn=onboarding-svc,ou=staff,dc=meridianbank,dc=internal}")
            String bindPrincipal,
            @Value("${app.directory.bind-credential:}") String bindCredential) {
        this.directoryUrl = directoryUrl;
        this.baseDn = baseDn;
        this.bindPrincipal = bindPrincipal;
        this.bindCredential = bindCredential;
    }

    /** Returns the directory entry for the employee reference, or an empty map if none is found. */
    public Map<String, String> searchDirectory(String employeeRef) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, directoryUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        SearchControls ctl = new SearchControls();
        ctl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctl.setReturningAttributes(new String[]{"uid", "cn", "mail"});
        try {
            InitialDirContext dirCtx = new InitialDirContext(env);
            NamingEnumeration<SearchResult> found = dirCtx.search(baseDn,
                    "(&(objectClass=person)(uid=" + employeeRef + "))", ctl);
            if (found.hasMore()) {
                Attributes attrs = found.next().getAttributes();
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("uid", attr(attrs, "uid"));
                entry.put("cn", attr(attrs, "cn"));
                entry.put("mail", attr(attrs, "mail"));
                return entry;
            }
        } catch (NamingException e) {
            return Map.of();
        }
        return Map.of();
    }

    /** Returns the staff entries for a department entered on the branch console form. */
    public List<Map<String, String>> searchDepartment(String department) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, directoryUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        SearchControls ctl = new SearchControls();
        ctl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctl.setReturningAttributes(new String[]{"uid", "cn", "mail"});
        List<Map<String, String>> entries = new ArrayList<>();
        try {
            InitialDirContext dirCtx = new InitialDirContext(env);
            NamingEnumeration<SearchResult> found = dirCtx.search(baseDn, "(department=" + department + ")", ctl);
            while (found.hasMore()) {
                Attributes attrs = found.next().getAttributes();
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("uid", attr(attrs, "uid"));
                entry.put("cn", attr(attrs, "cn"));
                entry.put("mail", attr(attrs, "mail"));
                entries.add(entry);
            }
        } catch (NamingException e) {
            return List.of();
        }
        return entries;
    }

    private String attr(Attributes attrs, String name) throws NamingException {
        var value = attrs.get(name);
        return value == null ? "" : String.valueOf(value.get());
    }
}
