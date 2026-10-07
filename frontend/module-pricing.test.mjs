import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'
const source=await readFile(new URL('./src/utils/modulePricing.ts',import.meta.url),'utf8')
const code=ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.ESNext,target:ts.ScriptTarget.ES2022}}).outputText
const { reconcileModulePricing,availableModuleRegions,pricingSnapshot }=await import(`data:text/javascript;base64,${Buffer.from(code).toString('base64')}`)
const regions=[
 {code:'CN',enabled:true,supportsAppStore:true,supportsGooglePlay:false,supportsIpadStore:true},
 {code:'US',enabled:true,supportsAppStore:true,supportsGooglePlay:true,supportsIpadStore:true},
 {code:'JP',enabled:false,supportsAppStore:true,supportsGooglePlay:true,supportsIpadStore:true}
]
const config={orderModuleId:11,orderType:'KEYWORD_INSTALL',allowedRegionCodes:['CN','US','JP'],regionPrices:{KEYWORD_INSTALL:{CN:'2.00',US:'1.50',JP:'3.00'}}}
test('store change removes stale regions and their prices from save payload with explicit notice',()=>{
 const result=reconcileModulePricing(config,{storeTypes:['GOOGLE_PLAY'],orderType:'KEYWORD_INSTALL'},regions)
 assert.deepEqual(result.removed,['CN','JP'])
 assert.deepEqual(result.config.allowedRegionCodes,['US'])
 assert.deepEqual(result.config.regionPrices,{KEYWORD_INSTALL:{US:1.5}})
 assert.deepEqual(config.allowedRegionCodes,['CN','US','JP'])
})
test('region supported by any selected store remains available',()=>{
 assert.deepEqual(availableModuleRegions({storeTypes:['APP_STORE','GOOGLE_PLAY'],orderType:'DOWNLOAD'},regions).map(r=>r.code),['CN','US'])
})
test('rating/review restrictions and empty selection remain consistent with server',()=>{
 for(const type of ['RATING','REVIEW']) assert.deepEqual(availableModuleRegions({storeTypes:['APP_STORE'],orderType:type},regions).map(r=>r.code),['US'])
 assert.deepEqual(reconcileModulePricing({...config,allowedRegionCodes:[]},{storeTypes:['APP_STORE'],orderType:'KEYWORD_INSTALL'},regions).config.regionPrices,{KEYWORD_INSTALL:{}})
})

test('restoring a default price clears dirty state despite null or reordered regions',()=>{
 const original={...config,allowedRegionCodes:['CN','US'],regionPrices:{KEYWORD_INSTALL:{US:1.5}}}
 const restored={...original,allowedRegionCodes:['US','CN'],regionPrices:{KEYWORD_INSTALL:{CN:null,US:1.5}}}
 assert.equal(pricingSnapshot(original),pricingSnapshot(restored))
 assert.notEqual(pricingSnapshot(original),pricingSnapshot({...restored,regionPrices:{KEYWORD_INSTALL:{CN:0.1,US:1.5}}}))
})
