/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Contributor;

import bee.util.RESTClient;

/**
 * @version 2017/01/10 3:15:42
 */
abstract class StandardVCS extends Github {

    /**
     * @param uri
     */
    protected StandardVCS(URI uri) {
        super(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String uri() {
        return uri.toASCIIString();
    }

    /**
     * <p>
     * Select standard VCS.
     * </p>
     * 
     * @param uri
     * @return
     */
    static Github of(URI uri) {
        switch (uri.getHost()) {
        case "github.com":
            return new GitHub(uri);

        default:
            return null;
        }
    }

    /**
     * @version 2017/01/16 16:27:07
     */
    static class GitHub extends StandardVCS {

        /**
         * @param uri
         */
        private GitHub(URI uri) {
            super(uri);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String issue() {
            return uri() + "/issues";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String uriForRead() {
            return "scm:git:" + uri() + ".git";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String uriForWrite() {
            return "scm:git:" + uri() + ".git";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Contributor> contributors() {
            RESTClient client = new RESTClient();
            return client.get("https://api.github.com/repos/teletha/bee/contributors", new GithubContributors())
                    .flatIterable(c -> c)
                    .flatMap(c -> client.get(c.url, new GitHubUser()))
                    .map(u -> {
                        Contributor contributor = new Contributor();
                        contributor.setEmail(u.email);
                        contributor.setName(u.name);
                        contributor.setUrl(u.html_url);
                        return contributor;
                    })
                    .skipError()
                    .toList();
        }

        /**
         * @version 2017/01/16 17:02:47
         */
        @SuppressWarnings("serial")
        private static class GithubContributors extends ArrayList<GitHubContributor> {
        }

        /**
         * @version 2017/01/16 17:00:23
         */
        private static class GitHubContributor {

            public String url;
        }

        /**
         * @version 2017/01/16 17:00:23
         */
        private static class GitHubUser {

            public String name;

            public String email;

            public String html_url;
        }
    }
}