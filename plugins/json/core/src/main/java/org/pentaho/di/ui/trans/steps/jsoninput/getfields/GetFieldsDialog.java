/*
 * Copyright 2018-2021 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.pentaho.di.ui.trans.steps.jsoninput.getfields;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.jsoninput.json.JsonSampler;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by hv-vamsi on 7/25/2018.
 */

public class GetFieldsDialog extends Dialog {

  private static final Class<?> PKG = GetFieldsDialog.class;
  private List<String> paths = new ArrayList<>();
  protected Button ok;
  protected Button cancel;
  protected Button clearSelection;
  private PropsUI props;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  public GetFieldsDialog( Shell parent ) {
    super( parent, SWT.NONE );
    this.props = PropsUI.getInstance();
  }

  public void open( String filename, List<String> paths, TableView wFields ) {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    if ( StringUtils.isBlank( filename ) ) {
      MessageBox mb = new MessageBox( parent, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "get-fields.plugin.app.unable-to-view.no-input.message" ) );
      mb.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.unable-to-view.label" ) );
      mb.open();
      return;
    }

    Shell shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    try {
      props.setLook( shell );
      FormLayout formLayout = new FormLayout();
      formLayout.marginWidth = Const.FORM_MARGIN;
      formLayout.marginHeight = Const.FORM_MARGIN;
      shell.setLayout( formLayout );
      shell.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.header.DialogTitle" ) );
      shell.setImage( LOGO );
      shell.setSize( 520, 700 );

      Monitor primary = parent.getMonitor();
      Rectangle bounds = primary.getBounds();
      Rectangle rect = shell.getClientArea();

      int x = bounds.x + ( bounds.width - rect.width ) / 2;
      int y = bounds.y + ( bounds.height - rect.height ) / 2;

      shell.setLocation( x, y );

      StyledText labelSelectFields = new StyledText( shell, SWT.NONE );
      props.setLook( labelSelectFields );
      labelSelectFields.setLayoutData( new FormDataBuilder().top( 1, 0 ).result() );
      String label = BaseMessages.getString( PKG, "get-fields-plugin.app.header.title" );
      labelSelectFields.setText( label );
      StyleRange style1 = new StyleRange();
      style1.start = 0;
      style1.length = label.length();
      style1.fontStyle = SWT.BOLD;
      labelSelectFields.setStyleRange( style1 );

      Text search = new Text( shell, SWT.SEARCH | SWT.ICON_CANCEL | SWT.CANCEL | SWT.ICON_SEARCH );
      props.setLook( search );
      search.setLayoutData( new FormDataBuilder().right( 100, 0 ).width( 200 ).result() );
      search.setMessage( BaseMessages.getString( PKG, "get-fields-plugin.app.header.search-parsed-fields.placeholder" ) );

      final Tree tree = new Tree( shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      props.setLook( tree );
      tree.setLayoutData( new FormDataBuilder().top( labelSelectFields, Const.MARGIN ).left( 0, 0 ).right( 100, 0 ).bottom( 100, -85 ).result() );

      JsonSampler jsonSampler = new JsonSampler();
      jsonSampler.sample( filename, tree );

      clearSelection = new Button( shell, SWT.PUSH );
      clearSelection.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.clear-selection.label" ) );
      props.setLook( clearSelection );
      BaseStepDialog.positionBottomButtons( shell, new Button[] { clearSelection }, 2, tree );

      Label separator = new Label( shell, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL );
      props.setLook( separator );
      separator.setLayoutData( new FormDataBuilder().top( clearSelection, Const.MARGIN ).fullWidth().result() );

      ok = new Button( shell, SWT.PUSH );
      ok.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.ok.button" ) );

      cancel = new Button( shell, SWT.PUSH );
      cancel.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.cancel.button" ) );

      BaseStepDialog.positionBottomRightButtons( shell, new Button[] { ok, cancel }, 2, separator );

      TreeItem item = tree.getItem( 0 );
      paths.forEach( path -> jsonSampler.selectByPath( path, item ) );

      setExpanded( item );

      tree.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {
          if ( event.detail == SWT.CHECK ) {
            TreeItem item = (TreeItem) event.item;
            boolean checked = item.getChecked();
            item.setChecked( checked );
            if ( Objects.isNull( item.getData( "Key" ) ) ) {
              checkItems( item, checked );
            }
          }
        }

        private void checkItems( TreeItem item, boolean checked ) {
          TreeItem[] items = item.getItems();
          for ( int i = 0; i < items.length; i++ ) {
            item.getItem( i ).setChecked( checked );
          }
        }
      } );

      search.addModifyListener( modifyEvent -> {
        boolean isSearchInvalid = doSearch( item, search.getText(), Boolean.TRUE );
        if ( isSearchInvalid ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "get-fields-plugin.app.unable-to-find.label" ) + " \"" + search.getText() + "\"" );
          mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
          mb.open();
        }
      } );

      Listener okListener = e -> ok( jsonSampler, tree, shell, wFields );
      ok.addListener( SWT.Selection, okListener );

      Listener cancelListener = e -> shell.dispose();
      cancel.addListener( SWT.Selection, cancelListener );

      Listener clearSelectionListener = e -> clearSelection( item );
      clearSelection.addListener( SWT.Selection, clearSelectionListener );

      shell.open();

      while ( !shell.isDisposed() ) {
        if ( !display.readAndDispatch() ) {
          display.sleep();
          break;
        }
      }
    } catch ( KettleFileException e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "get-fields.plugin.app.unable-to-access.message" ) );
      mb.setText( BaseMessages.getString( PKG, "get-fields-plugin.app.unable-to-access.label" ) );
      mb.open();
    } catch ( IOException e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "get-fields.plugin.app.unable-to-view.invalid.message" ) );
      mb.setText( BaseMessages.getString( PKG, "get-fields.plugin.app.unable-to-view.invalid.header" ) );
      mb.open();
    }
  }

  private boolean doSearch( TreeItem item, String data, boolean isSearchInvalid ) {
    if ( StringUtils.isNotBlank( data ) ) {
      if ( item.getText().contains( data ) ) {
        item.setBackground( GUIResource.getInstance().getColorLightBlue() );
        item.setForeground( GUIResource.getInstance().getColorBlack() );
        isSearchInvalid = Boolean.FALSE;
      } else {
        item.setForeground( GUIResource.getInstance().getColorGray() );
        item.setBackground( GUIResource.getInstance().getColorWhite() );
        if ( isSearchInvalid ) {
          isSearchInvalid = Boolean.TRUE;
        }
      }
    } else {
      item.setBackground( GUIResource.getInstance().getColorWhite() );
      item.setForeground( GUIResource.getInstance().getColorBlack() );
      isSearchInvalid = Boolean.FALSE;
    }
    for ( TreeItem child : item.getItems() ) {
      isSearchInvalid = doSearch( child, data, isSearchInvalid );
    }
    return isSearchInvalid;
  }

  private void setExpanded( TreeItem item ) {
    item.setExpanded( true );
    for ( TreeItem child : item.getItems() ) {
      setExpanded( child );
    }
  }

  private void clearSelection( TreeItem item ) {
    item.setChecked( false );
    for ( TreeItem child : item.getItems() ) {
      clearSelection( child );
    }
  }

  private void ok( JsonSampler jsonSampler, Tree tree, Shell shell, TableView wFields ) {
    paths.clear();
    paths.addAll( jsonSampler.getChecked( tree ) );

    wFields.table.setItemCount( paths.size() );
    if ( !paths.isEmpty() ) {
      for ( int i = 0; i < paths.size(); i++ ) {
        String path = paths.get( i );
        String[] values = path.split( ":" );
        TableItem item = wFields.table.getItem( i );
        item.setText( 1, values[0] );
        item.setText( 2, values[1] );
        item.setText( 3, values[2] );
      }
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    shell.dispose();
  }

}
