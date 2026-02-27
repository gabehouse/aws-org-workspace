#include "PluginProcessor.h"
#include "PluginEditor.h"

NewProjectAudioProcessorEditor::NewProjectAudioProcessorEditor(NewProjectAudioProcessor& p)
    : AudioProcessorEditor(&p), audioProcessor(p)
{
    // 1. Setup Sliders
    auto setupSlider = [this](juce::Slider& s, juce::Label& l, juce::String name) {
        s.setSliderStyle(juce::Slider::RotaryHorizontalVerticalDrag);
        s.setTextBoxStyle(juce::Slider::TextBoxBelow, false, 50, 20);
        s.setLookAndFeel(&customLNF);
        addAndMakeVisible(s);

        l.setText(name, juce::dontSendNotification);
        l.setJustificationType(juce::Justification::centred);
        addAndMakeVisible(l);
        };

    setupSlider(driveSlider, driveLabel, "Drive");
    setupSlider(mixSlider, mixLabel, "Mix");
    setupSlider(outputSlider, outputLabel, "Output");

    // 2. Attachments
    driveAttachment = std::make_unique<SliderAttachment>(audioProcessor.apvts, "DRIVE", driveSlider);
    mixAttachment = std::make_unique<SliderAttachment>(audioProcessor.apvts, "MIX", mixSlider);
    outputAttachment = std::make_unique<SliderAttachment>(audioProcessor.apvts, "OUT", outputSlider);

    // 3. DRIVE LOGIC (The Master)
    driveSlider.onValueChange = [this] {
        if (makeupButton.getToggleState()) {
            float driveValue = driveSlider.getValue();
            float driveDB = juce::Decibels::gainToDecibels(driveValue);

            // Calibrated to 0.85f for heavy tanh saturation
            float compDB = -(driveDB * 0.85f);
            float idealComp = juce::Decibels::decibelsToGain(compDB);

            outputSlider.setValue(idealComp + linkOffset, juce::dontSendNotification);

            audioProcessor.apvts.getParameter("OUT")->setValueNotifyingHost(
                audioProcessor.apvts.getParameterRange("OUT").convertTo0to1(outputSlider.getValue())
            );
        }
        };

    // 4. OUTPUT LOGIC (Dynamic Offset)
    outputSlider.onValueChange = [this] {
        if (makeupButton.getToggleState()) {
            if (!driveSlider.isMouseButtonDown()) {
                float driveDB = juce::Decibels::gainToDecibels(driveSlider.getValue());
                float idealComp = juce::Decibels::decibelsToGain(-(driveDB * 0.85f));
                linkOffset = outputSlider.getValue() - idealComp;
            }
        }
        };

    // 5. Auto-Makeup Setup
    makeupButton.setButtonText("Auto-Makeup");
    makeupButton.setToggleState(true, juce::dontSendNotification);
    makeupButton.setColour(juce::ToggleButton::tickColourId, juce::Colours::springgreen);
    addAndMakeVisible(makeupButton);

    makeupAttachment = std::make_unique<juce::AudioProcessorValueTreeState::ButtonAttachment>(
        audioProcessor.apvts, "MAKEUP", makeupButton);

    makeupButton.onClick = [this] {
        if (makeupButton.getToggleState()) {
            float driveDB = juce::Decibels::gainToDecibels(driveSlider.getValue());
            float idealComp = juce::Decibels::decibelsToGain(-(driveDB * 0.85f));
            linkOffset = outputSlider.getValue() - idealComp;
        }
        };

    // 6. Presets
    presetMenu.addItem("Init", 1);
    presetMenu.addItem("Warm Tube", 2);
    presetMenu.addItem("Acid Bite", 3);
    presetMenu.addItem("Destroy", 4);

    presetMenu.onChange = [this] {
        switch (presetMenu.getSelectedId()) {
        case 1: // Default / Clean
            loadPreset(1.0f, 0.5f, 1.0f);
            break;

        case 2: // Warm Tube (Subtle grit)
            loadPreset(3.5f, 0.5f, 1.0f);
            break;

        case 3: // Acid Bite (Aggressive harmonics)
            loadPreset(10.0f, 0.8f, 1.0f);
            break;

        case 4: // DESTROY (Pure square-wave chaos)
            loadPreset(20.0f, 1.0f, 1.0f);
            break;
        }
        };
    addAndMakeVisible(presetMenu);

    startTimerHz(30);
    setSize(500, 300);
}

NewProjectAudioProcessorEditor::~NewProjectAudioProcessorEditor()
{
    stopTimer();
    driveSlider.setLookAndFeel(nullptr);
    mixSlider.setLookAndFeel(nullptr);
    outputSlider.setLookAndFeel(nullptr);
}

void NewProjectAudioProcessorEditor::timerCallback()
{
    float level = audioProcessor.getLevel(0);

    // Pass the audio level to the LookAndFeel for the glowing sliders
    driveSlider.getProperties().set("level", level);
    mixSlider.getProperties().set("level", level);
    outputSlider.getProperties().set("level", level);

    repaint();
}

void NewProjectAudioProcessorEditor::paint(juce::Graphics& g)
{
    // 1. Premium Background
    juce::ColourGradient gradient(juce::Colours::darkgrey.darker(0.8f), 0, 0,
        juce::Colours::black, 0, (float)getHeight(), false);
    g.setGradientFill(gradient);
    g.fillAll();

    float rawLevel = audioProcessor.getLevel(0);
    // Use jmin to ensure sqrt doesn't behave weirdly if rawLevel is somehow > 1
    float visualLevel = std::sqrt(juce::jlimit(0.0f, 1.0f, rawLevel));

    juce::Colour dynamicColor;
    if (rawLevel >= 0.95f) {
        dynamicColor = juce::Colours::red;
    }
    else if (rawLevel >= 0.6f) {
        dynamicColor = juce::Colours::gold;
    }
    else {
        dynamicColor = juce::Colours::springgreen;
    }

    // 1. Safety Clamp for Title Brightness
    float brightness = 0.4f + (visualLevel * 0.6f);
    float safeTitleAlpha = juce::jlimit(0.0f, 1.0f, brightness); // THE FIX

    g.setColour(dynamicColor.withAlpha(safeTitleAlpha));
    g.setFont(juce::FontOptions(24.0f, juce::Font::bold));
    g.drawText("ACID SATURATOR", 25, 15, 250, 40, juce::Justification::left);

    // 2. Safety Clamp for Underglow
    if (visualLevel > 0.2f) {
        float glowIntensity = (visualLevel - 0.2f) * 0.5f;
        float safeGlowAlpha = juce::jlimit(0.0f, 1.0f, glowIntensity); // THE FIX

        g.setColour(dynamicColor.withAlpha(safeGlowAlpha));
        //  g.fillRoundedRectangle(20, 15, 210, 40, 5.0f);
    }
}

void NewProjectAudioProcessorEditor::resized()
{
    auto area = getLocalBounds();
    auto headerArea = area.removeFromTop(60);
    presetMenu.setBounds(headerArea.removeFromRight(150).reduced(10, 15));
    makeupButton.setBounds(headerArea.removeFromRight(120).reduced(0, 15));

    area.reduce(20, 10);
    auto sliderWidth = area.getWidth() / 3;

    auto driveArea = area.removeFromLeft(sliderWidth);
    driveLabel.setBounds(driveArea.removeFromTop(20));
    driveSlider.setBounds(driveArea);

    auto mixArea = area.removeFromLeft(sliderWidth);
    mixLabel.setBounds(mixArea.removeFromTop(20));
    mixSlider.setBounds(mixArea);

    auto outputArea = area;
    outputLabel.setBounds(outputArea.removeFromTop(20));
    outputSlider.setBounds(outputArea);
}

void NewProjectAudioProcessorEditor::loadPreset(float drive, float mix, float output)
{
    // 1. Set the Drive first. 
    // This triggers the onValueChange logic which calculates the 'Ideal' output.
    driveSlider.setValue(drive, juce::sendNotificationSync);

    // 2. Set the Mix.
    mixSlider.setValue(mix, juce::sendNotificationSync);

    // 3. IF Makeup is ON: We don't need to manually set 'output'.
    // The link logic has already moved the knob to the perfect 'level' spot.

    // IF Makeup is OFF: We move the output to the preset's manual value.
    if (!makeupButton.getToggleState())
    {
        outputSlider.setValue(output, juce::sendNotificationSync);
    }
}