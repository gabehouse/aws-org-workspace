#include "PluginProcessor.h"
#include "PluginEditor.h"

NewProjectAudioProcessor::NewProjectAudioProcessor()
    : AudioProcessor(BusesProperties().withInput("Input", juce::AudioChannelSet::stereo(), true)
        .withOutput("Output", juce::AudioChannelSet::stereo(), true)),
    oversampler(2, 2, juce::dsp::Oversampling<float>::filterHalfBandPolyphaseIIR, true)
{
    // Initialize atomic levels to zero
    rmsLevels[0].store(0.0f);
    rmsLevels[1].store(0.0f);
}

NewProjectAudioProcessor::~NewProjectAudioProcessor() {}

juce::AudioProcessorValueTreeState::ParameterLayout NewProjectAudioProcessor::createParameterLayout()
{
    juce::AudioProcessorValueTreeState::ParameterLayout layout;

    // Core Parameters
    layout.add(std::make_unique<juce::AudioParameterFloat>("DRIVE", "Drive", 1.0f, 20.0f, 1.0f));
    layout.add(std::make_unique<juce::AudioParameterFloat>("MIX", "Mix", 0.0f, 1.0f, 1.0f));
    layout.add(std::make_unique<juce::AudioParameterFloat>("OUT", "Output", 0.1f, 2.0f, 1.0f));

    // Auto-Makeup Toggle (The "Ableton" Style feature)
    layout.add(std::make_unique<juce::AudioParameterBool>("MAKEUP", "Auto-Makeup", true));

    return layout;
}

void NewProjectAudioProcessor::prepareToPlay(double sampleRate, int samplesPerBlock)
{
    oversampler.initProcessing(samplesPerBlock);

    driveSmoother.reset(sampleRate, 0.05);
    mixSmoother.reset(sampleRate, 0.05);
    outputSmoother.reset(sampleRate, 0.05);
}

void NewProjectAudioProcessor::processBlock(juce::AudioBuffer<float>& buffer, juce::MidiBuffer& midiMessages)
{
    juce::ScopedNoDenormals noDenormals;

    // 1. Update targets and check if Makeup is active
    driveSmoother.setTargetValue(apvts.getRawParameterValue("DRIVE")->load());
    mixSmoother.setTargetValue(apvts.getRawParameterValue("MIX")->load());
    outputSmoother.setTargetValue(apvts.getRawParameterValue("OUT")->load());

    // We check this once per block for efficiency
    bool makeupActive = *apvts.getRawParameterValue("MAKEUP") > 0.5f;

    juce::dsp::AudioBlock<float> block(buffer);

    // 2. OVERSAMPLING START
    juce::dsp::AudioBlock<float> upsampledBlock = oversampler.processSamplesUp(block);

    for (size_t channel = 0; channel < upsampledBlock.getNumChannels(); ++channel)
    {
        auto* channelData = upsampledBlock.getChannelPointer(channel);

        for (size_t sample = 0; sample < upsampledBlock.getNumSamples(); ++sample)
        {
            float input = channelData[sample];

            float drive = driveSmoother.getCurrentValue();
            float mix = mixSmoother.getCurrentValue();
            float output = outputSmoother.getCurrentValue();

            // 3. APPLY SATURATION
            float wet = std::tanh(input * drive);

            // 4. AUTO-MAKEUP LOGIC (The Level Matcher)
            if (makeupActive)
            {
                // We use a power of 0.85 to aggressively pull down high drive levels
                // This ensures "Destroy" (Drive 20) matches "Warm Tube" (Drive 2)
                float compensation = 1.0f / std::pow(drive, 0.3f);
                wet *= compensation;
            }

            // 5. MIX & FINAL GAIN
            channelData[sample] = ((wet * mix) + (input * (1.0f - mix))) * output;

            // 6. SAFETY LIMIT (Prevents digital overs at 4x rate)
            channelData[sample] = juce::jlimit(-1.0f, 1.0f, channelData[sample]);

            // Advance smoothers every 4th sample to stay in sync with native rate
            if (sample % 4 == 0)
            {
                driveSmoother.getNextValue();
                mixSmoother.getNextValue();
                outputSmoother.getNextValue();
            }
        }
    }

    // 7. DOWNSAMPLE
    oversampler.processSamplesDown(block);

    // 8. UPDATE METERS (Using native buffer magnitude)
    for (int i = 0; i < getTotalNumOutputChannels(); ++i)
    {
        // Store magnitude (0.0 to 1.0) for the editor's reactive title/glow
        rmsLevels[i].store(buffer.getMagnitude(i, 0, buffer.getNumSamples()));
    }
}

void NewProjectAudioProcessor::getStateInformation(juce::MemoryBlock& destData) {
    auto state = apvts.copyState();
    std::unique_ptr<juce::XmlElement> xml(state.createXml());
    copyXmlToBinary(*xml, destData);
}

void NewProjectAudioProcessor::setStateInformation(const void* data, int sizeInBytes) {
    std::unique_ptr<juce::XmlElement> xmlState(getXmlFromBinary(data, sizeInBytes));
    if (xmlState != nullptr) apvts.replaceState(juce::ValueTree::fromXml(*xmlState));
}

void NewProjectAudioProcessor::releaseResources() {}

juce::AudioProcessorEditor* NewProjectAudioProcessor::createEditor()
{
    return new NewProjectAudioProcessorEditor(*this);
}

juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter()
{
    return new NewProjectAudioProcessor();
}