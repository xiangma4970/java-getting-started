/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.SearchForm;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

	@RequestMapping("/herokuConTest")
	String herokuConTest(Map<String, Object> model){
	  try (Connection connection = dataSource.getConnection()) {
	    Statement stmt = connection.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM salesforce.HerokuCon__c");

	    ArrayList<String> output = new ArrayList<String>();
	    while (rs.next()) {
			Double bd = rs.getDouble("goods1Value__c");
			if (bd == 0) {
				output.add("Herokuコネクトサンプル名：" + rs.getString("Name") + "　　　" + "商品１：" + rs.getString("goods1__c")+ "　　　" + "商品１価格：" + rs.getString("goods1Value__c"));
			}
			else {
				bd = bd * 1.08;
				String bds = String.valueOf(bd);
				output.add("Herokuコネクトサンプル名：" + rs.getString("Name") + "　　　" + "商品１：" + rs.getString("goods1__c")+ "　　　" + "商品１価格：" + rs.getString("goods1Value__c")+ "　　　" + "商品１価格(税込)：" + bds);
			}
	    }

	    model.put("records", output);
	    return "herokuConTest";
	  } catch (Exception e) {
	    model.put("message", e.getMessage());
	    return "error";
	  }
	}

	@RequestMapping("/search")
	public String search(@ModelAttribute SearchForm form, Model model){

		String strShohin1 = form.getVal1();
		String strShohin1val = form.getVal2();
		
		String sql = "SELECT * FROM salesforce.HerokuCon__c";
		
		if (!isNullOrEnpty(strShohin1) && !isNullOrEnpty(strShohin1val)) {
			sql += " where goods1__c like " + "'%" + strShohin1 + "%'" + " goods1Value__c like " + "'%" + strShohin1val + "%'";
		}
		
		ArrayList<String> output = new ArrayList<String>();

		try (Connection connection = dataSource.getConnection()) {
		    Statement stmt = connection.createStatement();
		    ResultSet rs = stmt.executeQuery(sql);

		    while (rs.next()) {
				Double bd = rs.getDouble("goods1Value__c");
				if (bd == 0) {
					output.add("Herokuコネクトサンプル名：" + rs.getString("Name") + "　　　" + "商品１：" + rs.getString("goods1__c")+ "　　　" + "商品１価格：" + rs.getString("goods1Value__c"));
				}
				else {
					bd = bd * 1.08;
					String bds = String.valueOf(bd);
					output.add("Herokuコネクトサンプル名：" + rs.getString("Name") + "　　　" + "商品１：" + rs.getString("goods1__c")+ "　　　" + "商品１価格：" + rs.getString("goods1Value__c")+ "　　　" + "商品１価格(税込)：" + bds);
				}
		    }

		  } catch (Exception e) {
		    output.add("DBerror" + sql);
		  }

        //--------------------------------------
        // 画面表示項目設定
        //--------------------------------------
        // 画面に渡す値を設定
        model.addAttribute("form", form);
        model.addAttribute("records", output);

        // search画面を表示
        return "search";
    }

    private Boolean isNullOrEnpty(String val){

        if(null == val || "".equals(val)){
            return true;
        }
        return false;
    }

}
