package mybatis.diagnostics.mapperStatementDiagnostics;

import mybatis.diagnostics.MapperStatementDiagnostics;
import mybatis.diagnostics.event.GroupEvent;
import mybatis.diagnostics.exception.TypeCompatibilityCheckException;
import mybatis.diagnostics.model.DiagnosticSource;
import mybatis.diagnostics.model.DiagnosticType;
import mybatis.diagnostics.model.context.Context;
import mybatis.parser.XMLConfigParser;
import mybatis.parser.model.Config;
import mybatis.project.ConfigNotFoundException;
import mybatis.project.MyBatisProjectInitializationException;
import mybatis.project.MyBatisProjectService;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.ibatis.session.ExecutorType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class CheckTypeCompatibilityTest {
    Logger logger = LoggerFactory.getLogger(CheckTypeCompatibilityTest.class);

    static Config config;
    Connection connection;

    @AfterEach
    void clean() throws SQLException {
        if(connection!= null)
            connection.close();
    }

    @BeforeAll
    static void setup() throws ConfigNotFoundException, IOException, URISyntaxException, SQLException, MyBatisProjectInitializationException {
        var root = Paths.get(ClassLoader.getSystemClassLoader().getResource("examples/mybatis-app1").toURI()).normalize();
        var server = new MyBatisProjectService();
        server.initialize(root, "h2");
        var path = server.getConfigFile();

        var parser = new XMLConfigParser(Files.newInputStream(path), server);
        config = parser.parse();
        var env = config.getEnvironment();
        var manager = env.getTransactionManager();
        var transaction = manager
                .getTransactionFactory()
                .newTransaction(env.getDataSourceConfig().getDataSource(), null, false);
        var exec = config.newExecutor(transaction, ExecutorType.SIMPLE);
        var connection = transaction.getConnection();
        var mapper = config.getMappedStatement("db.BlogMapper.createTableIfNotExist");
        var pstmt = connection.prepareStatement(mapper.getSqlSource().getBoundSql(new HashMap()).toString());
        pstmt.execute();
        connection.close();
    }

    @Nested
    @DisplayName("Insert Into Statement")
    class InsertIntoStatementTest {
        @Test
        void test() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.InsertStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }

        @Test
        void test1() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.WrongInsertStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
    }

    @Nested
    @DisplayName("Insert Select Statement")
    class InsertSelectStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.InsertSelectStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.WrongInsertSelectStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
    }

    @Nested
    @DisplayName("Select Join Where Statement")
    class SelectJoinWhereStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.SelectJoinStatement");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.WrongSelectStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 2);
        }
    }

    @Nested
    @DisplayName("Select Union Statement")
    class SelectUnionStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.SelectUnionStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.WrongSelectUnionStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 2);
        }
    }

    @Nested
    @DisplayName("Select Into Statement")
    class SelectIntoStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.SelectIntoStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.WrongSelectIntoStatement1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
    }


    @Nested
    @DisplayName("Update Statement")
    class UpdateStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.updateContent1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.wrongUpdateContent1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
        @Test
        void test2() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.wrongUpdateContent2");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
    }



    @Nested
    @DisplayName("Delete Statement")
    class DeleteStatementTest {
        @Test
        void test() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.deleteContent");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 0);
        }
        @Test
        void test1() throws TypeCompatibilityCheckException {
            var stmt = config.getMappedStatement("db.WrongType.wrongDeleteContent1");
            var diag = new MapperStatementDiagnostics();
            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
            logger.info(result.toString());
            Assertions.assertEquals(result.build().getErrorList().size(), 1);
        }
    }




//
//        @Nested
//    @DisplayName("Select Statement")
//    class SelectStatementTest {
//        @Test
//        void test1() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.select1");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 0);
//        }
//
//        @Test
//        void test2() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.select2");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 1);
//        }
//
//        @Test
//        void test3() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.select3");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 1);
//        }
//    }
//    @Nested
//    @DisplayName("Delete Statement")
//    class DeleteStatementTest {
//        @Test
//        void test1() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.delete1");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 0);
//        }
//
//        @Test
//        void test2() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.delete2");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 1);
//        }
//        @Test
//        void test3() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.delete3");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 2);
//        }
//    }
//    @Nested
//    @DisplayName("Update Statement")
//    class UpdateStatementTest {
//        @Test
//        void test1() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.update1");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 0);
//        }
//
//        @Test
//        void test2() throws SQLException, JSQLParserException, TypeCompatibilityCheckException {
//            var stmt = config.getMappedStatement("db.WrongType.update2");
//            var diag = new MapperStatementDiagnostics();
//            var result = diag.checkTypeCompatibility(stmt, new GroupEvent(new Context<>(new DiagnosticSource(DiagnosticType.MapperStatement), stmt)));
//            logger.info(result.toString());
//            Assertions.assertEquals(resul.build().getErrorList()t.size(), 3);
//        }
//    }
}