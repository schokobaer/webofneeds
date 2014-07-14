/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.protocol.util.linkeddata;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import won.protocol.rest.LinkedDataRestClient;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LinkedDataSource implementation that uses an ehcache for caching.
 */
@Qualifier("default")
public class CachingLinkedDataSource implements LinkedDataSource, InitializingBean
{
  private static final String CACHE_NAME = "linkedDataCache";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Autowired(required = true)
  private EhCacheCacheManager cacheManager;
  private LinkedDataRestClient linkedDataRestClient;
  private Ehcache cache;

  //In-memory dataset for caching linked data.


  public Model getModelForResource(URI resource){

    assert resource != null : "resource must not be null";
    Element element = cache.get(resource);
    Object model = element.getObjectValue();
    if (model instanceof Model) return (Model) model;
    throw new IllegalStateException(
        new MessageFormat("The underlying linkedDataCache should only contain Models, but we got a {0} for URI {1}")
            .format(new Object[]{model.getClass(), resource}));
  }

  @Override
  public Model getModelForResource(final URI resourceUri, List<URI> properties, List<URI> objects,
                                   int maxRequest, int maxDepth) {
    Set<URI> crawledURIs = new HashSet<URI>();
    Set<URI> newlyDiscoveredURIs = new HashSet<URI>();
    Set<URI> urisToCrawl = null;
    newlyDiscoveredURIs.add(resourceUri);
    int depth = 0;
    int requests = 0;

    Model model = getModelForResource(resourceUri);


    OUTER: while (newlyDiscoveredURIs.size() > 0 && depth < maxDepth && requests < maxRequest){
      urisToCrawl = newlyDiscoveredURIs;
      newlyDiscoveredURIs = new HashSet<URI>();
      for (URI currentURI: urisToCrawl) {
        //add all models from urisToCrawl
        Model currentModel =  getModelForResource(currentURI);
        model.add(currentModel);
        newlyDiscoveredURIs.addAll(getURIsToCrawl(currentModel, crawledURIs, properties, objects));
        crawledURIs.add(currentURI);
        requests++;
        logger.debug("current Request: "+requests);
        if (requests >= maxRequest) break OUTER;

      }
      depth++;
      logger.debug("current Depth: "+depth);
    }
    return model;
  }


  private Set<URI> getURIsToCrawl(Model model, Set<URI> crawled,  List<URI> properties, List<URI> objects) {
    Set<URI> toCrawl = new HashSet<>();
    for (int i = 0; i<properties.size();i++){
      Property p = model.createProperty(properties.get(i).toString());
      NodeIterator objectIterator = model.listObjectsOfProperty(p);
      for (;objectIterator.hasNext();){
        RDFNode objectNode = objectIterator.next();

        if (objectNode.isURIResource()) {
          URI discoveredUri = URI.create(objectNode.asResource().getURI());
          if (!crawled.contains(discoveredUri)){
            toCrawl.add(discoveredUri);
          }
        }
      }

    }
    return toCrawl;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Ehcache baseCache = cacheManager.getCacheManager().getCache(CACHE_NAME);
    this.cache = new SelfPopulatingCache(baseCache, new LinkedDataCacheEntryFactory());
  }

  public void setCacheManager(final EhCacheCacheManager cacheManager)
  {
    this.cacheManager = cacheManager;
  }

  public void setLinkedDataRestClient(final LinkedDataRestClient linkedDataRestClient) {
    this.linkedDataRestClient = linkedDataRestClient;
  }

  private class LinkedDataCacheEntryFactory implements CacheEntryFactory {
    private LinkedDataCacheEntryFactory(){}

    @Override
    public Object createEntry(final Object key) throws Exception
    {
      if (key instanceof URI) {
        logger.debug("fetching linked data for URI {}", key);
        return linkedDataRestClient.readResourceData((URI) key);
      } else {
        throw new IllegalArgumentException("this cache only resolves URIs to Models");
      }
    }
  }
}