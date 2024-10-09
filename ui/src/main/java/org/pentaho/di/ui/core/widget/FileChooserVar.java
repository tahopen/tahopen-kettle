/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;

/**
 * Provides a composite containing a TextVar, Variable Icon, and browse button.  The browse button will open the file
 * chooser.
 */
public class FileChooserVar extends Composite {
  private TextVar wPath;
  private Button wBrowseButton;
  VariableSpace space;
  protected ObjectLocationSpecificationMethod specificationMethod;
  PropsUI props = PropsUI.getInstance();

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel ) {
    this( space, composite, flags, buttonLabel, null, null, null );
  }

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel, String toolTipText ) {
    this( space, composite, flags, buttonLabel, toolTipText, null, null );
  }

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel,
                         GetCaretPositionInterface getCaretPositionInterface,
                         InsertTextInterface insertTextInterface ) {
    this( space, composite, flags, buttonLabel, null, getCaretPositionInterface, insertTextInterface );
  }

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel, String toolTipText,
                         GetCaretPositionInterface getCaretPositionInterface,
                         InsertTextInterface insertTextInterface ) {
    super( composite, SWT.NONE );
    this.space = space;
    initialize( flags, buttonLabel, toolTipText, getCaretPositionInterface, insertTextInterface );
  }

  protected void initialize( int flags, String buttonLabel,
                             String toolTipText, GetCaretPositionInterface getCaretPositionInterface,
                             InsertTextInterface insertTextInterface ) {

    props.setLook( this );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;
    this.setLayout( formLayout );

    this.wBrowseButton = new Button( this, SWT.PUSH );
    props.setLook( wBrowseButton );
    wBrowseButton.setText( buttonLabel );
    FormData fdButton = new FormData();
    fdButton.top = new FormAttachment( 0, 0 );
    fdButton.right = new FormAttachment( 100, 0 );
    wBrowseButton.setLayoutData( fdButton );

    wPath = new TextVar( space, this, flags, toolTipText, getCaretPositionInterface, insertTextInterface );
    props.setLook( wPath );
    FormData fdTextVar = new FormData();
    fdTextVar.top = new FormAttachment( 0, 0 );
    fdTextVar.left = new FormAttachment( 0, 0 );
    fdTextVar.right = new FormAttachment( wBrowseButton, -8 );
    wPath.setLayoutData( fdTextVar );

    wBrowseButton.addListener( SWT.Selection, event -> openFileBrowser() );
  }

  public TextVar getTextVarWidget() {
    return wPath;
  }

  public String getText() {
    return wPath.getText();
  }

  public Button getButton() {
    return wBrowseButton;
  }

  private void openFileBrowser() {
    LogChannel log = new LogChannel();
    TransMeta meta = new TransMeta( space );
    SelectionAdapterFileDialogTextVar selectionAdapterFileDialogTextVar =
      new SelectionAdapterFileDialogTextVar( log, wPath, meta,
        new SelectionAdapterOptions( SelectionOperation.FILE,
          new FilterType[] { FilterType.ALL }, FilterType.ALL ) );
    selectionAdapterFileDialogTextVar.widgetSelected( null );
    if ( wPath.getText() != null && Const.isWindows() ) {
      wPath.setText( wPath.getText().replace( '\\', '/' ) );
    }
  }

  public void addModifyListener( ModifyListener modifyListener ) {
    wPath.addModifyListener( modifyListener );
  }

  public void setText( String text ) {
    wPath.setText( text );
  }

}

