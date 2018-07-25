package sybrix.easygsp2.security

import groovy.sql.Sql
import org.apache.derby.jdbc.EmbeddedDataSource
import org.slf4j.LoggerFactory
import sybrix.easygsp2.util.PropertiesFile
import sybrix.easygsp2.util.StringUtil

import javax.sql.DataSource

class AuthorizationValidator {
        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AuthorizationValidator.class);
        private static DataSource dataSource

        public static void init(PropertiesFile propertiesFile) {
                if (propertiesFile.getBoolean("jwt.controller.enabled", false)) {
                        return
                } else {
                        logger.debug("jwt.controller.enabled = false, skipping init of AuthorizationValidator")
                }
                String url = propertiesFile.getString("database.url2")

                EmbeddedDataSource ds = new EmbeddedDataSource()
                ds.setDatabaseName(url);
                ds.setUser(propertiesFile.getString("database.username"))
                ds.setPassword(propertiesFile.getString("database.password"))

                dataSource = ds
                try {
                        ds.getConnection().close()
                } catch (Exception e) {
                        logger.error("datasource connction test failed. url: ${url}, message: ${e.message}")
                }
        }

        public static Client addClient(String name) {
                String clientId = StringUtil.randomCode(16)
                String clientSecret = JwtUtil.instance.generateKey()

                def sql = new Sql(dataSource)
                List<List<Object>> result = sql.executeInsert("INSERT INTO auth_client(id, client_id, client_secret, client_name) VALUES ((select max(id) + 1 FROM AUTH_CLIENT), ${clientId}, ${clientSecret}, ${name})")
                if (result) {
                        return getClientByClientId(clientId)
                }

                sql.close()

        }

        public static void updateClient(Client client) {

                def sql = new Sql(dataSource)

                sql.executeUpdate("UPDATE auth_client SET client_id=${client.clientId}, client_secret=${client.clientSecret}, client_name=${client.name} WHERE id = ${client.id}")

                sql.close()

        }


        public static void addScopes(String clientId, List<String> scopes) {

                def sql = new Sql(dataSource)
                sql.withBatch {
                        scopes.each { scope ->
                                sql.executeUpdate("DELETE FROM auth_client_scope WHERE client_id IN (select id FROM auth_client WHERE client_id = ${clientId}) and scope=${scope}")
                        }

                        scopes.each { scope ->
                                sql.executeInsert("INSERT INTO auth_client_scope(id, client_id, scope) VALUES ((select max(id) + 1 FROM auth_client_scope), ${clientId}, ${scope})")
                        }
                }

                sql.close()
        }

        public static void removeScope(String clientId, String scope) {

                def sql = new Sql(dataSource)

                sql.executeUpdate("DELETE FROM auth_client_scope WHERE client_id IN (select id FROM auth_client WHERE client_id = ${clientId}) and scope=${scope}")

                sql.close()
        }

        public static void addScope(String clientId, String scope) {
                addScopes(clientId, [scope])
        }

        public static void deleteToken(Long clientId, String refreshToken) {

                def sql = new Sql(dataSource)

                sql.executeUpdate("DELETE FROM auth_client_token WHERE client_id=${clientId} and refresh_token=${refreshToken}")

                sql.close()
        }

        public static Client getClientByClientId(String clientId) {
                def sql = new Sql(dataSource)
                def scopes = []

                def result = sql.firstRow("SELECT id, client_id clientId, client_secret clientSecret, client_name clientName FROM auth_client_scope WHERE client_id in (select id FROM auth_client WHERE client_id = ${clientId})")
                Client client = new Client(id:result.get("id"), clientId: result.get("clientId"), clientSecret: result.get("clientSecret"), clientName:result.get("clientName"))

                sql.close()

                client
        }

        public static ClientCode insertToken(Long clientId, String refreshToken, String username) {

                def sql = new Sql(dataSource)

                List<List<Object>> result = sql.executeInsert("INSERT INTO auth_client_token(id, client_id, refresh_token, username) VALUES ((select max(id) + 1 FROM auth_client_token), ${clientId}, ${refreshToken}, ${username})")
                if (result){

                }
                sql.close()
        }

        public static ClientCode getToken(String clientId) {
                def sql = new Sql(dataSource)
                def scopes = []

                sql.rows("SELECT scope FROM auth_client_scope WHERE client_id in (select id FROM auth_client WHERE client_id = ${clientId})").each {
                        scopes.add(it.scope)
                }
                sql.close()

                scopes
        }


        public static List<String> getScopes(String clientId) {
                def sql = new Sql(dataSource)
                def scopes = []

                sql.rows("SELECT scope FROM auth_client_scope WHERE client_id in (select id FROM auth_client WHERE client_id = ${clientId})").each {
                        scopes.add(it.scope)
                }
                sql.close()

                scopes
        }

        public static ClientCode refreshToken(String clientId, String refreshToken, String username) {
                def sql = new Sql(dataSource)
                def row = sql.firstRow("SELECT client_id clientId, client_secret clientSecret FROM auth_client WHERE client_id=${clientId}")

                if (row) {
                        return new Client(clientId: row.clientId, clientSecret: row.clientSecret)
                }
                sql.close()
        }

}


class Client {
        Long id
        String clientId
        String clientSecret
        String name
}

class ClientScope {
        String clientId
        String scope
}

class ClientCode {
        String clientId
        String username
        String code
        String refreshToken
}
