package org.example.user;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class UserDAO {
    private final DSLContext dslContext;

    public UserDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public boolean userExists(String username, String email) {
        return dslContext.fetchExists(
                DSL.selectOne()
                        .from(USERS)
                        .where(USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email)))
        );
    }


    public void insertUser(String username, String hashedPassword, String email) {
        dslContext.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD, hashedPassword)
                .set(USERS.EMAIL, email)
                .set(USERS.BALANCE, BigDecimal.ZERO)
                .set(USERS.ROLE, "user")
                .execute();
    }
}
