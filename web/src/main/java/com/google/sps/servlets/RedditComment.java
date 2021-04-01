// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@WebServlet("/store-comments")
public class RedditComment extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query =
        Query.newEntityQueryBuilder()
            .setKind("Comments-url")
            .setOrderBy(OrderBy.desc("url"))
            .build();
    QueryResults<Entity> savedUrls = datastore.run(query);

    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Comments");

    while (savedUrls.hasNext()) {
      Entity entity = savedUrls.next();
      String url = entity.getString("url");

      try {
        Document RedditDiscussions = Jsoup.connect(url).get();

        System.out.printf("\nTitle: %s\n", RedditDiscussions.title());

        Elements commentSection = RedditDiscussions.getElementsByClass("_3cjCphgls6DH-irkVaA0GM");

        for (Element comments : commentSection) {
          String comment = comments.getElementsByClass("_1qeIAgB0cPwnLhDF9XSiJM").text();
          FullEntity RedditComment =
              Entity.newBuilder(keyFactory.newKey()).set("comment", comment).build();
          datastore.put(RedditComment);
        }
      } catch (Exception e) {
        System.out.println("error loading url");
      }
    }
  }
}
