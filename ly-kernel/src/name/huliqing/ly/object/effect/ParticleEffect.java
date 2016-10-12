/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.effect;

import com.jme3.effect.Particle;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import name.huliqing.ly.data.EffectData;
import name.huliqing.ly.data.EntityData;
import name.huliqing.ly.object.Loader;
import name.huliqing.ly.object.emitter.Emitter;
import name.huliqing.ly.object.scene.Scene;

/**
 *
 * @author huliqing
 */
public class ParticleEffect extends Effect {
    private String emitter;
    // 是否立即执行所有粒子发射
    private boolean emitAll;
    // 是否随机颜色
    private boolean randomColor;
    // 粒子是否使用世界坐标位置，默认true,与粒子的默认设置相同
    private boolean inWorldSpace = true;
    // 粒子材质的混合模式，默认AlphaAdditive,这意味着粒子图片中的黑色背
    // 景会被过滤掉。这会导致无法创建黑色烟雾的效果，这个时候可以使用
    // Alpha模式来代替。
    private BlendMode blendMode = BlendMode.AlphaAdditive;
    
    // -- 内部
    private ParticleEmitter pe;

    @Override
    public void setData(EffectData data) {
        super.setData(data); 
        emitter = data.getAsString("emitter", emitter);
        emitAll = data.getAsBoolean("emitAll", emitAll);
        randomColor = data.getAsBoolean("randomColor", randomColor);
        inWorldSpace = data.getAsBoolean("inWorldSpace", inWorldSpace);
        String tempBlendMode = data.getAsString("blendMode");
        if (tempBlendMode != null) {
            blendMode = BlendMode.valueOf(tempBlendMode);
        }
    }
    
    @Override
    public void initialize(Scene scene) {
        super.initialize(scene);
        
        if (pe == null) {
            Emitter em = Loader.load(emitter);
            if (randomColor) {
                pe = em.getParticleEmitter(new RandomColorEmitter());
            } else {
                pe = em.getParticleEmitter();
            }
            pe.setInWorldSpace(inWorldSpace);
            pe.getMaterial().getAdditionalRenderState().setBlendMode(blendMode);
            animRoot.attachChild(pe);
            
            pe.setQueueBucket(Bucket.Translucent);
        }
        if (emitAll) {
            pe.emitAllParticles();
        }
    }


    @Override
    public void cleanup() {
        if (pe != null) {
            pe.killAllParticles();
        }
        super.cleanup();
    }
    
    //------------- get and set
    
    // 主要是为了随机颜色。
    private class RandomColorEmitter extends ParticleEmitter {
        
        public RandomColorEmitter() {
            super("MyEmitter", ParticleMesh.Type.Triangle, 0);
        }
        
        @Override
        public void setParticleInfluencer(ParticleInfluencer particleInfluencer) {
            super.setParticleInfluencer(particleInfluencer); 
        }

        @Override
        public void emitAllParticles() {
            super.emitAllParticles();
            changeColor();
        }

        @Override
        public void updateFromControl(float tpf) {
            super.updateFromControl(tpf);
            changeColor();
        }
        
        private void changeColor() {
            if (this.isEnabled()) {
                Particle[] particles = this.getParticles();
                if (particles != null && particles.length > 0) {
                    for (Particle p : particles) {
                        if (p.life <= 0) { 
                            continue;
                        }
                        p.color.set(FastMath.nextRandomFloat(), FastMath.nextRandomFloat(), FastMath.nextRandomFloat(), 1);
                    }
                }                
            }
        }
    }
}
