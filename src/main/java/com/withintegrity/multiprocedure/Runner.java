package com.withintegrity.multiprocedure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.Datum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.AbstractSqlTypeValue;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Component
public class Runner implements CommandLineRunner {

    DataSource ds;

    DataSource ds2;

    @Override
    public void run(String... args) throws Exception {
        SqlTypeValue qwekqwek = FoodMapper.mapFoodsToArray(new Food("Qwekqwek", 20), new Food("Fishball", 10));

        SimpleJdbcCall jdbc = new SimpleJdbcCall(ds)
                .withProcedureName("SP_IN_RPS")
                .declareParameters(
                        new SqlParameter("FOODLISTIN", OracleTypes.ARRAY, "FOODLIST"),
                        new SqlOutParameter("FOODLISTOUT", OracleTypes.ARRAY, "FOODLIST")
                );

        MapSqlParameterSource source = new MapSqlParameterSource().addValue("FOODLISTIN", qwekqwek);
        Map<String, Object> execute = jdbc.execute(source);

        System.out.println("SP1 >>>>>>>>>>>>");
        Datum[] oracleArray = ((ARRAY) execute.get("FOODLISTOUT")).getOracleArray();
        Arrays.stream(Food.from(oracleArray)).forEach(System.out::println);

        ////////////////////////// SECOND SP FROM ANOTHER DATABASE

        Food[] from = Food.from(oracleArray);
        SqlTypeValue foodArray = FoodMapper.mapFoodsToArray(from);

        SimpleJdbcCall jdbc2 = new SimpleJdbcCall(ds2)
                .withProcedureName("SP_IN_RPS")
                .declareParameters(
                        new SqlParameter("FOODLISTIN", OracleTypes.ARRAY, "FOODLIST"),
                        new SqlOutParameter("FOODLISTOUT", OracleTypes.ARRAY, "FOODLIST")
                );


        MapSqlParameterSource source2 = new MapSqlParameterSource().addValue("FOODLISTIN", foodArray);
        Map<String, Object> execute2 = jdbc2.execute(source2);

        Datum[] oracleArray2 = ((ARRAY) execute2.get("FOODLISTOUT")).getOracleArray();
        System.out.println("SP2 >>>>>>>>>>>>");
        Arrays.stream(Food.from(oracleArray2)).forEach(System.out::println);

    }

    // USED FOR TABLE, OR ARRAY TYPES
    public static class FoodMapper {
        public static SqlTypeValue mapFoodsToArray(SQLData... items) {
            return new AbstractSqlTypeValue() {
                protected Object createTypeValue(Connection conn, int sqlType, String typeName) throws SQLException {
                    ArrayDescriptor arrayDescriptor = new ArrayDescriptor(typeName, conn);
                    ARRAY idArray = new ARRAY(arrayDescriptor, conn, items);
                    return idArray;
                }
            };
        }

    }

    // USED FOR STRUCT TYPES
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Food implements SQLData {
        private String name;
        private Integer age;

        @Override
        public String getSQLTypeName() throws SQLException {
            return "FOOD";
        }

        @Override
        public void readSQL(SQLInput stream, String typeName) throws SQLException {
            setName(stream.readString());
            setAge(stream.readInt());
        }

        @Override
        public void writeSQL(SQLOutput stream) throws SQLException {
            stream.writeString(getName());
            stream.writeInt(getAge());
        }

        public static Food from(Datum item) throws SQLException {
            Object[] attributes = ((Struct) item).getAttributes();
            return new Food((String) attributes[0], ((BigDecimal) attributes[1]).intValue());
        }

        public static Food[] from(Datum[] item) {
            Function<Datum, Food> datumFoodFunction = obj -> {
                try {
                    return from(obj);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };

            return Arrays.stream(item)
                    .map(datumFoodFunction)
                    .toArray(Food[]::new);
        }
    }

    public static class FoodRowMapper implements RowMapper<Food> {

        @Override
        public Food mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Food(rs.getString("NAME"), rs.getInt("AGE"));
        }
    }
}
